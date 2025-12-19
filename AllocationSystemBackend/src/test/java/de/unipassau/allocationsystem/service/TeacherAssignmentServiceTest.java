package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.entity.AcademicYear;
import de.unipassau.allocationsystem.entity.AllocationPlan;
import de.unipassau.allocationsystem.entity.InternshipType;
import de.unipassau.allocationsystem.entity.Subject;
import de.unipassau.allocationsystem.entity.Teacher;
import de.unipassau.allocationsystem.entity.TeacherAssignment;
import de.unipassau.allocationsystem.exception.DuplicateResourceException;
import de.unipassau.allocationsystem.repository.TeacherAssignmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeacherAssignmentServiceTest {

    @Mock
    private TeacherAssignmentRepository teacherAssignmentRepository;

    @Mock
    private CreditHourTrackingService creditHourTrackingService;

    @InjectMocks
    private TeacherAssignmentService teacherAssignmentService;

    private AllocationPlan plan;
    private Teacher teacher;
    private InternshipType internshipType;
    private Subject subject;

    @BeforeEach
    void setUp() {
        plan = new AllocationPlan();
        plan.setId(1L);
        plan.setStatus(AllocationPlan.PlanStatus.DRAFT);
        AcademicYear year = new AcademicYear();
        year.setId(2L);
        plan.setAcademicYear(year);

        teacher = new Teacher();
        teacher.setId(3L);
        teacher.setEmploymentStatus(Teacher.EmploymentStatus.ACTIVE);

        internshipType = new InternshipType();
        internshipType.setId(4L);

        subject = new Subject();
        subject.setId(5L);
        subject.setIsActive(true);
    }

    @Test
    void create_Success() {
        TeacherAssignment entity = new TeacherAssignment();
        entity.setAllocationPlan(plan);
        entity.setTeacher(teacher);
        entity.setInternshipType(internshipType);
        entity.setSubject(subject);
        entity.setStudentGroupSize(1);

        when(teacherAssignmentRepository.existsByAllocationPlanIdAndTeacherIdAndInternshipTypeIdAndSubjectId(
                eq(1L), eq(3L), eq(4L), eq(5L)))
                .thenReturn(false);

        when(teacherAssignmentRepository.save(any(TeacherAssignment.class))).thenAnswer(inv -> {
            TeacherAssignment t = inv.getArgument(0);
            t.setId(10L);
            return t;
        });

        TeacherAssignment saved = teacherAssignmentService.create(entity);

        assertNotNull(saved);
        assertEquals(10L, saved.getId());
        verify(teacherAssignmentRepository).save(any(TeacherAssignment.class));
        verify(creditHourTrackingService).recalculateForTeacherAndYear(eq(3L), eq(2L));
    }

    @Test
    void create_Duplicate_Throws() {
        TeacherAssignment entity = new TeacherAssignment();
        entity.setAllocationPlan(plan);
        entity.setTeacher(teacher);
        entity.setInternshipType(internshipType);
        entity.setSubject(subject);

        when(teacherAssignmentRepository.existsByAllocationPlanIdAndTeacherIdAndInternshipTypeIdAndSubjectId(
                eq(1L), eq(3L), eq(4L), eq(5L)))
                .thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> teacherAssignmentService.create(entity));
        verify(teacherAssignmentRepository, never()).save(any());
        verify(creditHourTrackingService, never()).recalculateForTeacherAndYear(anyLong(), anyLong());
    }

    @Test
    void update_Success_RecalcTriggered() {
        TeacherAssignment existing = new TeacherAssignment();
        existing.setId(20L);
        existing.setAllocationPlan(plan);
        existing.setTeacher(teacher);
        existing.setStudentGroupSize(1);

        when(teacherAssignmentRepository.findById(20L)).thenReturn(Optional.of(existing));
        when(teacherAssignmentRepository.save(any(TeacherAssignment.class))).thenAnswer(i -> i.getArgument(0));

        TeacherAssignment updateData = new TeacherAssignment();
        updateData.setStudentGroupSize(3);

        TeacherAssignment updated = teacherAssignmentService.update(20L, updateData);

        assertNotNull(updated);
        verify(teacherAssignmentRepository).save(any(TeacherAssignment.class));
        verify(creditHourTrackingService).recalculateForTeacherAndYear(eq(3L), eq(2L));
    }

    @Test
    void delete_Success_RecalcTriggered() {
        TeacherAssignment existing = new TeacherAssignment();
        existing.setId(30L);
        existing.setAllocationPlan(plan);
        existing.setTeacher(teacher);

        when(teacherAssignmentRepository.findById(30L)).thenReturn(Optional.of(existing));
        doNothing().when(teacherAssignmentRepository).delete(existing);

        teacherAssignmentService.delete(30L);

        verify(teacherAssignmentRepository).delete(existing);
        verify(creditHourTrackingService).recalculateForTeacherAndYear(eq(3L), eq(2L));
    }
}
