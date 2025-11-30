package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.dto.teacherassignment.TeacherAssignmentCreateDto;
import de.unipassau.allocationsystem.dto.teacherassignment.TeacherAssignmentUpdateDto;
import de.unipassau.allocationsystem.entity.*;
import de.unipassau.allocationsystem.mapper.TeacherAssignmentMapper;
import de.unipassau.allocationsystem.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TeacherAssignmentServiceTest {

    @Mock
    private TeacherAssignmentRepository teacherAssignmentRepository;

    @Mock
    private AllocationPlanRepository allocationPlanRepository;

    @Mock
    private TeacherRepository teacherRepository;

    @Mock
    private InternshipTypeRepository internshipTypeRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private TeacherAssignmentMapper mapper;

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

        // default mapper behavior for response DTO
        when(mapper.toResponseDto(any())).thenAnswer(inv -> {
            TeacherAssignment t = inv.getArgument(0);
            de.unipassau.allocationsystem.dto.teacherassignment.TeacherAssignmentResponseDto r = new de.unipassau.allocationsystem.dto.teacherassignment.TeacherAssignmentResponseDto();
            if (t != null) {
                r.setId(t.getId());
                if (t.getAllocationPlan() != null) r.setPlanId(t.getAllocationPlan().getId());
                if (t.getTeacher() != null) r.setTeacherId(t.getTeacher().getId());
            }
            return r;
        });
    }

    @Test
    void create_Success() {
        TeacherAssignment taEntity = new TeacherAssignment();
        taEntity.setId(10L);
        when(allocationPlanRepository.findById(1L)).thenReturn(Optional.of(plan));
        when(teacherRepository.findById(3L)).thenReturn(Optional.of(teacher));
        when(internshipTypeRepository.findById(4L)).thenReturn(Optional.of(internshipType));
        when(subjectRepository.findById(5L)).thenReturn(Optional.of(subject));
        when(teacherAssignmentRepository.existsByAllocationPlanIdAndTeacherIdAndInternshipTypeIdAndSubjectId(1L, 3L, 4L, 5L)).thenReturn(false);
        when(mapper.toEntity(any(), any(), any(), any(), any())).thenReturn(taEntity);
        when(mapper.toResponseDto(any())).thenAnswer(inv -> {
            TeacherAssignment t = inv.getArgument(0);
            de.unipassau.allocationsystem.dto.teacherassignment.TeacherAssignmentResponseDto r = new de.unipassau.allocationsystem.dto.teacherassignment.TeacherAssignmentResponseDto();
            r.setId(t.getId());
            if (t.getAllocationPlan() != null) r.setPlanId(t.getAllocationPlan().getId());
            if (t.getTeacher() != null) r.setTeacherId(t.getTeacher().getId());
            return r;
        });
        when(teacherAssignmentRepository.save(any(TeacherAssignment.class))).thenAnswer(invocation -> {
            TeacherAssignment t = invocation.getArgument(0);
            t.setId(10L);
            return t;
        });

        TeacherAssignmentCreateDto dto = new TeacherAssignmentCreateDto();
        dto.setTeacherId(3L);
        dto.setInternshipTypeId(4L);
        dto.setSubjectId(5L);
        dto.setStudentGroupSize(1);

        var resp = teacherAssignmentService.create(1L, dto, true);

        assertNotNull(resp);
        assertEquals(10L, resp.getId());
        verify(teacherAssignmentRepository).save(any(TeacherAssignment.class));
        verify(creditHourTrackingService).recalculateForTeacherAndYear(eq(3L), eq(2L));
    }

    @Test
    void create_Duplicate_Throws() {
        when(allocationPlanRepository.findById(1L)).thenReturn(Optional.of(plan));
        when(teacherRepository.findById(3L)).thenReturn(Optional.of(teacher));
        when(internshipTypeRepository.findById(4L)).thenReturn(Optional.of(internshipType));
        when(subjectRepository.findById(5L)).thenReturn(Optional.of(subject));
        when(teacherAssignmentRepository.existsByAllocationPlanIdAndTeacherIdAndInternshipTypeIdAndSubjectId(1L, 3L, 4L, 5L)).thenReturn(true);

        TeacherAssignmentCreateDto dto = new TeacherAssignmentCreateDto();
        dto.setTeacherId(3L);
        dto.setInternshipTypeId(4L);
        dto.setSubjectId(5L);

        assertThrows(IllegalStateException.class, () -> teacherAssignmentService.create(1L, dto, true));
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

        TeacherAssignmentUpdateDto dto = new TeacherAssignmentUpdateDto();
        dto.setStudentGroupSize(3);

        var res = teacherAssignmentService.update(1L, 20L, dto, true);

        assertNotNull(res);
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

        teacherAssignmentService.delete(1L, 30L, true);

        verify(teacherAssignmentRepository).delete(existing);
        verify(creditHourTrackingService).recalculateForTeacherAndYear(eq(3L), eq(2L));
    }
}
