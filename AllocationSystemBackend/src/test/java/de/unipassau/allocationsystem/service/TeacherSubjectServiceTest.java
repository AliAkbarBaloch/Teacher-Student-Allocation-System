package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.entity.AcademicYear;
import de.unipassau.allocationsystem.entity.Subject;
import de.unipassau.allocationsystem.entity.Teacher;
import de.unipassau.allocationsystem.entity.TeacherSubject;
import de.unipassau.allocationsystem.exception.DuplicateResourceException;
import de.unipassau.allocationsystem.repository.AcademicYearRepository;
import de.unipassau.allocationsystem.repository.SubjectRepository;
import de.unipassau.allocationsystem.repository.TeacherRepository;
import de.unipassau.allocationsystem.repository.TeacherSubjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link TeacherSubjectService}.
 * <p>
 * This test class validates teacher-subject association CRUD operations and duplicate detection.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class TeacherSubjectServiceTest {

    @Mock
    private TeacherSubjectRepository teacherSubjectRepository;

    @Mock
    private AcademicYearRepository academicYearRepository;

    @Mock
    private TeacherRepository teacherRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @InjectMocks
    private TeacherSubjectService teacherSubjectService;

    private AcademicYear year;
    private Teacher teacher;
    private Subject subject;

    @BeforeEach
    void setUp() {
        year = new AcademicYear();
        year.setId(1L);

        teacher = new Teacher();
        teacher.setId(2L);
        teacher.setEmploymentStatus(Teacher.EmploymentStatus.ACTIVE);

        subject = new Subject();
        subject.setId(3L);
        subject.setIsActive(true);
    }

    @Test
    void createSuccess() {
        TeacherSubject entity = TeacherSubject.builder()
                .academicYear(year)
                .teacher(teacher)
                .subject(subject)
                .availabilityStatus("AVAILABLE")
                .gradeLevelFrom(5)
                .gradeLevelTo(10)
                .build();

        when(academicYearRepository.findById(1L)).thenReturn(Optional.of(year));
        when(teacherRepository.findById(2L)).thenReturn(Optional.of(teacher));
        when(subjectRepository.findById(3L)).thenReturn(Optional.of(subject));
        when(teacherSubjectRepository.findByTeacherIdAndAcademicYearId(2L, 1L)).thenReturn(List.of());
        when(teacherSubjectRepository.save(any(TeacherSubject.class))).thenAnswer(invocation -> {
            TeacherSubject ts = invocation.getArgument(0);
            ts.setId(10L);
            return ts;
        });

        TeacherSubject created = teacherSubjectService.create(entity);

        assertNotNull(created);
        assertEquals(10L, created.getId());
        verify(teacherSubjectRepository).save(any(TeacherSubject.class));
    }

    @Test
    void createDuplicateThrows() {
        TeacherSubject entity = TeacherSubject.builder()
                .academicYear(year)
                .teacher(teacher)
                .subject(subject)
                .availabilityStatus("AVAILABLE")
                .build();

        TeacherSubject existing = TeacherSubject.builder()
                .id(5L)
                .academicYear(year)
                .teacher(teacher)
                .subject(subject)
                .build();

        when(academicYearRepository.findById(1L)).thenReturn(Optional.of(year));
        when(teacherRepository.findById(2L)).thenReturn(Optional.of(teacher));
        when(subjectRepository.findById(3L)).thenReturn(Optional.of(subject));
        when(teacherSubjectRepository.findByTeacherIdAndAcademicYearId(2L, 1L))
                .thenReturn(List.of(existing));

        assertThrows(DuplicateResourceException.class, () -> teacherSubjectService.create(entity));
    }
}
