package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.dto.teacher.TeacherCreateDto;
import de.unipassau.allocationsystem.dto.teacher.TeacherResponseDto;
import de.unipassau.allocationsystem.dto.teacher.TeacherUpdateDto;
import de.unipassau.allocationsystem.entity.School;
import de.unipassau.allocationsystem.entity.School.SchoolType;
import de.unipassau.allocationsystem.entity.Teacher;
import de.unipassau.allocationsystem.entity.Teacher.EmploymentStatus;
import de.unipassau.allocationsystem.entity.Teacher.UsageCycle;
import de.unipassau.allocationsystem.exception.DuplicateResourceException;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.mapper.TeacherMapper;
import de.unipassau.allocationsystem.repository.SchoolRepository;
import de.unipassau.allocationsystem.repository.TeacherRepository;
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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TeacherService aligned with the provided TeacherService implementation.
 */
@ExtendWith(MockitoExtension.class)
class TeacherServiceTest {

    @Mock
    private TeacherRepository teacherRepository;

    @Mock
    private SchoolRepository schoolRepository;

    @Mock
    private TeacherMapper teacherMapper;

    @InjectMocks
    private TeacherService teacherService;

    private School testSchool;
    private Teacher testTeacher;
    private TeacherCreateDto createDto;
    private TeacherUpdateDto updateDto;
    private TeacherResponseDto responseDto;

    @BeforeEach
    void setUp() {
        testSchool = new School();
        testSchool.setId(1L);
        testSchool.setSchoolName("Test School");
        testSchool.setSchoolType(SchoolType.PRIMARY);
        testSchool.setZoneNumber(1);
        testSchool.setIsActive(true);

        testTeacher = new Teacher();
        testTeacher.setId(1L);
        testTeacher.setSchool(testSchool);
        testTeacher.setFirstName("John");
        testTeacher.setLastName("Doe");
        testTeacher.setEmail("john.doe@school.de");
        testTeacher.setPhone("+49841123456");
        testTeacher.setIsPartTime(false);
        testTeacher.setEmploymentStatus(EmploymentStatus.ACTIVE);
        testTeacher.setUsageCycle(UsageCycle.FLEXIBLE);

        createDto = new TeacherCreateDto();
        createDto.setSchoolId(1L);
        createDto.setFirstName("Jane");
        createDto.setLastName("Smith");
        createDto.setEmail("jane.smith@school.de");
        createDto.setPhone("+49841654321");
        createDto.setIsPartTime(false);
        createDto.setEmploymentStatus(EmploymentStatus.ACTIVE);
        createDto.setUsageCycle(UsageCycle.FLEXIBLE);

        updateDto = new TeacherUpdateDto();
        updateDto.setFirstName("Updated");
        updateDto.setLastName("Name");
        updateDto.setEmail("updated@school.de");

        responseDto = TeacherResponseDto.builder()
                .id(1L)
                .schoolId(1L)
                .schoolName("Test School")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@school.de")
                .phone("+49841123456")
                .isPartTime(false)
                .employmentStatus(EmploymentStatus.ACTIVE)
                .usageCycle(UsageCycle.FLEXIBLE)
                .employmentStatus(EmploymentStatus.ACTIVE)
                .build();
    }

    @Test
    void getAll_ReturnsMappedList() {
        when(teacherRepository.findAll()).thenReturn(List.of(testTeacher));
        when(teacherMapper.toResponseDto(testTeacher)).thenReturn(responseDto);

        List<TeacherResponseDto> result = teacherService.getAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(responseDto, result.get(0));
        verify(teacherRepository).findAll();
        verify(teacherMapper).toResponseDto(testTeacher);
    }

    @Test
    void getPaginated_Default_Success() {
        Page<Teacher> page = new PageImpl<>(Collections.singletonList(testTeacher));
        Map<String, String> queryParams = new HashMap<>();

        when(teacherRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        Map<String, Object> resp = teacherService.getPaginated(queryParams, null);

        assertNotNull(resp);
        assertTrue(resp.containsKey("items"));
        assertTrue(resp.containsKey("totalItems"));
        verify(teacherRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getPaginated_WithSearch_Success() {
        Page<Teacher> page = new PageImpl<>(Collections.singletonList(testTeacher));
        Map<String, String> queryParams = new HashMap<>();
        when(teacherRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        Map<String, Object> resp = teacherService.getPaginated(queryParams, "John");

        assertNotNull(resp);
        verify(teacherRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getById_WhenExists_ReturnsOptionalDto() {
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(testTeacher));
        when(teacherMapper.toResponseDto(testTeacher)).thenReturn(responseDto);

        Optional<TeacherResponseDto> result = teacherService.getById(1L);

        assertTrue(result.isPresent());
        assertEquals(responseDto, result.get());
        verify(teacherRepository).findById(1L);
        verify(teacherMapper).toResponseDto(testTeacher);
    }

    @Test
    void getById_WhenNotFound_ReturnsEmptyOptional() {
        when(teacherRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<TeacherResponseDto> result = teacherService.getById(99L);

        assertTrue(result.isEmpty());
        verify(teacherRepository).findById(99L);
    }

    @Test
    void createTeacher_Success() {
        Teacher toSave = new Teacher();
        when(teacherRepository.existsByEmail(createDto.getEmail())).thenReturn(false);
        when(schoolRepository.findById(1L)).thenReturn(Optional.of(testSchool));
        when(teacherMapper.toEntityCreate(createDto)).thenReturn(toSave);
        when(teacherRepository.save(any(Teacher.class))).thenAnswer(inv -> {
            Teacher t = inv.getArgument(0);
            t.setId(2L);
            return t;
        });
        when(teacherMapper.toResponseDto(any(Teacher.class))).thenReturn(responseDto);

        TeacherResponseDto result = teacherService.createTeacher(createDto);

        assertNotNull(result);
        verify(teacherRepository).existsByEmail(createDto.getEmail());
        verify(schoolRepository).findById(1L);
        verify(teacherRepository).save(any(Teacher.class));
        verify(teacherMapper).toResponseDto(any(Teacher.class));
    }

    @Test
    void createTeacher_DuplicateEmail_ThrowsException() {
        when(teacherRepository.existsByEmail(createDto.getEmail())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> teacherService.createTeacher(createDto));
        verify(teacherRepository).existsByEmail(createDto.getEmail());
        verify(teacherRepository, never()).save(any());
    }

    @Test
    void createTeacher_SchoolNotFound_ThrowsException() {
        when(teacherRepository.existsByEmail(createDto.getEmail())).thenReturn(false);
        when(schoolRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> teacherService.createTeacher(createDto));
        verify(teacherRepository).existsByEmail(createDto.getEmail());
        verify(schoolRepository).findById(1L);
        verify(teacherRepository, never()).save(any());
    }

    @Test
    void updateTeacher_Success() {
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(testTeacher));
        when(teacherRepository.existsByEmailAndIdNot(updateDto.getEmail(), 1L)).thenReturn(false);
        doNothing().when(teacherMapper).updateEntityFromDto(any(TeacherUpdateDto.class), any(Teacher.class));
        when(teacherRepository.save(any(Teacher.class))).thenReturn(testTeacher);
        when(teacherMapper.toResponseDto(testTeacher)).thenReturn(responseDto);

        TeacherResponseDto result = teacherService.updateTeacher(1L, updateDto);

        assertNotNull(result);
        verify(teacherRepository).findById(1L);
        verify(teacherMapper).updateEntityFromDto(any(TeacherUpdateDto.class), any(Teacher.class));
        verify(teacherRepository).save(any(Teacher.class));
    }

    @Test
    void updateTeacher_NotFound_ThrowsException() {
        when(teacherRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> teacherService.updateTeacher(99L, updateDto));
        verify(teacherRepository).findById(99L);
        verify(teacherRepository, never()).save(any());
    }

    @Test
    void updateTeacher_DuplicateEmail_ThrowsException() {
        updateDto.setEmail("existing@school.de");
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(testTeacher));
        when(teacherRepository.existsByEmailAndIdNot("existing@school.de", 1L)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> teacherService.updateTeacher(1L, updateDto));
        verify(teacherRepository).findById(1L);
        verify(teacherRepository).existsByEmailAndIdNot("existing@school.de", 1L);
        verify(teacherRepository, never()).save(any());
    }

    @Test
    void updateTeacher_ChangeSchool_Success() {
        School newSchool = new School();
        newSchool.setId(2L);
        newSchool.setSchoolName("New School");
        newSchool.setIsActive(true);

        updateDto.setSchoolId(2L);
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(testTeacher));
        when(schoolRepository.findById(2L)).thenReturn(Optional.of(newSchool));
        doNothing().when(teacherMapper).updateEntityFromDto(any(TeacherUpdateDto.class), any(Teacher.class));
        when(teacherRepository.save(any(Teacher.class))).thenReturn(testTeacher);
        when(teacherMapper.toResponseDto(testTeacher)).thenReturn(responseDto);

        TeacherResponseDto result = teacherService.updateTeacher(1L, updateDto);

        assertNotNull(result);
        verify(schoolRepository).findById(2L);
        verify(teacherRepository).save(any(Teacher.class));
    }

    @Test
    void deleteTeacher_Success() {
        when(teacherRepository.existsById(1L)).thenReturn(true);

        teacherService.deleteTeacher(1L);

        verify(teacherRepository).existsById(1L);
        verify(teacherRepository).deleteById(1L);
    }

    @Test
    void deleteTeacher_NotFound_ThrowsException() {
        when(teacherRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> teacherService.deleteTeacher(99L));
        verify(teacherRepository).existsById(99L);
        verify(teacherRepository, never()).deleteById(anyLong());
    }

    @Test
    void helperMethods_existAndSortFields() {
        when(teacherRepository.existsById(1L)).thenReturn(true);

        assertTrue(teacherService.existsById(1L));

        List<Map<String, String>> fields = teacherService.getSortFields();
        assertNotNull(fields);
        assertTrue(fields.stream().anyMatch(m -> "id".equals(m.get("key"))));
        assertTrue(fields.stream().anyMatch(m -> "firstName".equals(m.get("key"))));
    }
}
