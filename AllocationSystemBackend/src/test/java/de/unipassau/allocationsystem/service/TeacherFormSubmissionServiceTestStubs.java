package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.entity.AcademicYear;
import de.unipassau.allocationsystem.entity.Teacher;
import de.unipassau.allocationsystem.repository.AcademicYearRepository;
import de.unipassau.allocationsystem.repository.TeacherRepository;

import java.util.Optional;

import static org.mockito.Mockito.when;

final class TeacherFormSubmissionServiceTestStubs {

    private TeacherFormSubmissionServiceTestStubs() {
    }

    static void teacherFound(TeacherRepository teacherRepository, long teacherId, Teacher teacher) {
        when(teacherRepository.findById(teacherId)).thenReturn(Optional.of(teacher));
    }

    static void teacherNotFound(TeacherRepository teacherRepository, long teacherId) {
        when(teacherRepository.findById(teacherId)).thenReturn(Optional.empty());
    }

    static void yearFound(AcademicYearRepository academicYearRepository, long yearId, AcademicYear academicYear) {
        when(academicYearRepository.findById(yearId)).thenReturn(Optional.of(academicYear));
    }

    static void yearNotFound(AcademicYearRepository academicYearRepository, long yearId) {
        when(academicYearRepository.findById(yearId)).thenReturn(Optional.empty());
    }
}
