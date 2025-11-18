package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.entity.School;
import de.unipassau.allocationsystem.entity.School.SchoolType;
import de.unipassau.allocationsystem.exception.DuplicateResourceException;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.repository.SchoolRepository;
import de.unipassau.allocationsystem.utils.PaginationUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchoolServiceTest {

    @Mock
    private SchoolRepository schoolRepository;

    @InjectMocks
    private SchoolService schoolService;

    private School testSchool;
    private School createSchool;
    private School updatePayload;

    @BeforeEach
    void setUp() {
        testSchool = new School();
        testSchool.setId(1L);
        testSchool.setSchoolName("Test Elementary School");
        testSchool.setSchoolType(SchoolType.PRIMARY);
        testSchool.setZoneNumber(1);
        testSchool.setAddress("Test Street 1");
        testSchool.setLatitude(new BigDecimal("48.5734053"));
        testSchool.setLongitude(new BigDecimal("13.4579944"));
        testSchool.setDistanceFromCenter(new BigDecimal("2.5"));
        testSchool.setTransportAccessibility("Bus Line 1");
        testSchool.setContactEmail("test@school.de");
        testSchool.setContactPhone("+49841123456");
        testSchool.setIsActive(true);

        createSchool = new School();
        createSchool.setSchoolName("New Test School");
        createSchool.setSchoolType(SchoolType.MIDDLE);
        createSchool.setZoneNumber(2);
        createSchool.setAddress("New Street 10");

        updatePayload = new School();
        updatePayload.setSchoolName("Updated School Name");
        updatePayload.setAddress("Updated Address");
        updatePayload.setZoneNumber(5);
    }

    @Test
    void getAll_ReturnsAllSchools() {
        when(schoolRepository.findAll()).thenReturn(Arrays.asList(testSchool));

        List<School> result = schoolService.getAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testSchool, result.get(0));
        verify(schoolRepository).findAll();
    }

    @Test
    void getPaginated_ReturnsFormattedPagination() {
        Page<School> page = new PageImpl<>(Arrays.asList(testSchool));
        Map<String, String> queryParams = Map.of();

        when(schoolRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        Map<String, Object> resp = schoolService.getPaginated(queryParams, null);

        assertNotNull(resp);
        assertTrue(resp.containsKey("items"));
        assertTrue(resp.containsKey("totalItems"));
        verify(schoolRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getById_WhenExists_ReturnsOptional() {
        when(schoolRepository.findById(1L)).thenReturn(Optional.of(testSchool));

        Optional<School> result = schoolService.getById(1L);

        assertTrue(result.isPresent());
        assertEquals(testSchool, result.get());
        verify(schoolRepository).findById(1L);
    }

    @Test
    void create_WhenNameUnique_SavesAndReturns() {
        when(schoolRepository.findBySchoolName(createSchool.getSchoolName())).thenReturn(Optional.empty());
        when(schoolRepository.save(createSchool)).thenAnswer(inv -> {
            School s = inv.getArgument(0);
            s.setId(2L);
            return s;
        });

        School saved = schoolService.create(createSchool);

        assertNotNull(saved);
        assertEquals(2L, saved.getId());
        verify(schoolRepository).findBySchoolName(createSchool.getSchoolName());
        verify(schoolRepository).save(createSchool);
    }

    @Test
    void create_WhenDuplicate_ThrowsDuplicateResourceException() {
        when(schoolRepository.findBySchoolName(createSchool.getSchoolName())).thenReturn(Optional.of(testSchool));

        assertThrows(DuplicateResourceException.class, () -> schoolService.create(createSchool));

        verify(schoolRepository).findBySchoolName(createSchool.getSchoolName());
        verify(schoolRepository, never()).save(any());
    }

    @Test
    void update_WhenExists_UpdatesAndSaves() {
        when(schoolRepository.findById(1L)).thenReturn(Optional.of(testSchool));
        when(schoolRepository.findBySchoolName(updatePayload.getSchoolName())).thenReturn(Optional.empty());
        when(schoolRepository.save(any(School.class))).thenAnswer(inv -> inv.getArgument(0));

        School updated = schoolService.update(1L, updatePayload);

        assertNotNull(updated);
        assertEquals("Updated School Name", updated.getSchoolName());
        assertEquals(Integer.valueOf(5), updated.getZoneNumber());
        verify(schoolRepository).findById(1L);
        verify(schoolRepository).findBySchoolName(updatePayload.getSchoolName());
        verify(schoolRepository).save(any(School.class));
    }

    @Test
    void update_WhenNotFound_ThrowsResourceNotFoundException() {
        when(schoolRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> schoolService.update(999L, updatePayload));

        verify(schoolRepository).findById(999L);
        verify(schoolRepository, never()).save(any());
    }

    @Test
    void delete_WhenExists_DeletesById() {
        when(schoolRepository.existsById(1L)).thenReturn(true);

        schoolService.delete(1L);

        verify(schoolRepository).existsById(1L);
        verify(schoolRepository).deleteById(1L);
    }

    @Test
    void delete_WhenNotFound_ThrowsResourceNotFoundException() {
        when(schoolRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> schoolService.delete(999L));

        verify(schoolRepository).existsById(999L);
        verify(schoolRepository, never()).deleteById(anyLong());
    }

    @Test
    void helperMethods_existenceChecksAndSortFields() {
        when(schoolRepository.findBySchoolName("Test Elementary School")).thenReturn(Optional.of(testSchool));
        when(schoolRepository.findById(1L)).thenReturn(Optional.of(testSchool));

        assertTrue(schoolService.schoolNameExists("Test Elementary School"));
        assertTrue(schoolService.existsById(1L));

        List<Map<String, String>> fields = schoolService.getSortFields();
        List<String> keys = schoolService.getSortFieldKeys();

        assertNotNull(fields);
        assertTrue(keys.containsAll(Arrays.asList("id", "schoolName", "createdAt", "updatedAt")));
    }
}
