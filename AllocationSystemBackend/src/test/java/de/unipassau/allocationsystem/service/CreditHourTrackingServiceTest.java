package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.entity.AcademicYear;
import de.unipassau.allocationsystem.entity.CreditHourTracking;
import de.unipassau.allocationsystem.entity.School;
import de.unipassau.allocationsystem.entity.Teacher;
import de.unipassau.allocationsystem.entity.TeacherAssignment;
import de.unipassau.allocationsystem.mapper.CreditHourTrackingMapper;
import de.unipassau.allocationsystem.repository.CreditHourTrackingRepository;
import de.unipassau.allocationsystem.repository.TeacherAssignmentRepository;
import de.unipassau.allocationsystem.repository.TeacherRepository;
import de.unipassau.allocationsystem.repository.AcademicYearRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link CreditHourTrackingService}.
 * <p>
 * This test class validates credit hour tracking calculations and recalculations.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class CreditHourTrackingServiceTest {

    @Mock
    private CreditHourTrackingRepository repository;
    @Mock
    private TeacherAssignmentRepository teacherAssignmentRepository;
    @Mock
    private TeacherRepository teacherRepository;
    @Mock
    private AcademicYearRepository academicYearRepository;
    @Mock
    private CreditHourTrackingMapper mapper;

    @InjectMocks
    private CreditHourTrackingService service;

    private Teacher teacher;
    private AcademicYear year;

    @BeforeEach
    void setUp() {
        teacher = new Teacher();
        School school = new School();
        school.setSchoolType(School.SchoolType.PRIMARY);
        teacher.setSchool(school);

        year = new AcademicYear();
        year.setTotalCreditHours(100);
        year.setElementarySchoolHours(10);
        year.setMiddleSchoolHours(20);
    }

    @Test
    void recalculateForTeacherAndYear_createsOrUpdatesRecord() {
        Long teacherId = 1L;
        Long yearId = 2L;

        when(teacherRepository.findById(teacherId)).thenReturn(Optional.of(teacher));
        when(academicYearRepository.findById(yearId)).thenReturn(Optional.of(year));

        TeacherAssignment a1 = new TeacherAssignment();
        a1.setAssignmentStatus(TeacherAssignment.AssignmentStatus.PLANNED);
        TeacherAssignment a2 = new TeacherAssignment();
        a2.setAssignmentStatus(TeacherAssignment.AssignmentStatus.CONFIRMED);
        TeacherAssignment a3 = new TeacherAssignment();
        a3.setAssignmentStatus(TeacherAssignment.AssignmentStatus.CANCELLED);

        when(teacherAssignmentRepository.findByTeacherIdAndYearId(teacherId, yearId)).thenReturn(List.of(a1, a2, a3));
        when(repository.findByTeacherIdAndAcademicYearId(teacherId, yearId)).thenReturn(Optional.empty());

        ArgumentCaptor<CreditHourTracking> captor = ArgumentCaptor.forClass(CreditHourTracking.class);
        when(repository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        service.recalculateForTeacherAndYear(teacherId, yearId);

        verify(repository, times(1)).save(any());
        CreditHourTracking saved = captor.getValue();
        assertThat(saved.getAssignmentsCount()).isEqualTo(2);
        // hoursPerAssignment should be elementarySchoolHours (10.0) for PRIMARY
        assertThat(saved.getCreditHoursAllocated()).isEqualTo(2 * 10.0);
        assertThat(saved.getCreditBalance()).isEqualTo(100.0 - (2 * 10.0));
    }
}
