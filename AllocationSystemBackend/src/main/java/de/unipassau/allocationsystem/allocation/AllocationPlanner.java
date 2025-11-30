package de.unipassau.allocationsystem.allocation;

import de.unipassau.allocationsystem.repository.AcademicYearRepository;
import de.unipassau.allocationsystem.repository.SubjectCategoryRepository;
import de.unipassau.allocationsystem.repository.SubjectRepository;
import de.unipassau.allocationsystem.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Profile("!test")
public class AllocationPlanner implements CommandLineRunner {

    private final AcademicYearRepository academicYearRepository;
    private final SubjectCategoryRepository subjectCategoryRepository;
    private final SubjectRepository subjectRepository;
    private final TeacherRepository teacherRepository;

    @Override
    public void run(String... args) {
//        AcademicYear academicYear = academicYearRepository.findById(1L).get();
//        List<SubjectCategory> subjectCategories = subjectCategoryRepository.findAll();
//
//        for (SubjectCategory subjectCategory : subjectCategories) {
//            System.out.println(subjectCategory.getCategoryTitle());
//        }
    }
}
