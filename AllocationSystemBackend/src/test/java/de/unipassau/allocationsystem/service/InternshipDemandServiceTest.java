package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.repository.InternshipDemandAggregation;
import de.unipassau.allocationsystem.repository.InternshipDemandRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InternshipDemandServiceTest {

    @Mock
    private InternshipDemandRepository repository;

    // other dependencies can be mocked but are not used by the tested method
    @Mock
    private de.unipassau.allocationsystem.repository.InternshipTypeRepository internshipTypeRepository;
    @Mock
    private de.unipassau.allocationsystem.repository.SubjectRepository subjectRepository;
    @Mock
    private de.unipassau.allocationsystem.repository.AcademicYearRepository academicYearRepository;
    @Mock
    private de.unipassau.allocationsystem.mapper.InternshipDemandMapper mapper;

    @InjectMocks
    private InternshipDemandService service;

    @Test
    void getAggregationForYear_returnsMappedDtos() {
        Long yearId = 2025L;

        InternshipDemandAggregation a1 = new InternshipDemandAggregation() {
            @Override
            public Long getInternshipTypeId() { return 1L; }

            @Override
            public Integer getTotalRequiredTeachers() { return 10; }
        };

        InternshipDemandAggregation a2 = new InternshipDemandAggregation() {
            @Override
            public Long getInternshipTypeId() { return 2L; }

            @Override
            public Integer getTotalRequiredTeachers() { return 5; }
        };

        when(repository.aggregateByYear(yearId)).thenReturn(List.of(a1, a2));

        var dtos = service.getAggregationForYear(yearId);

        assertEquals(2, dtos.size());
        assertEquals(1L, dtos.get(0).getInternshipTypeId());
        assertEquals(10, dtos.get(0).getTotalRequiredTeachers());
        assertEquals(2L, dtos.get(1).getInternshipTypeId());
        assertEquals(5, dtos.get(1).getTotalRequiredTeachers());
    }
}
