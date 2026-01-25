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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link TeacherService}.
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
        testSchool = buildActiveSchool(1L, "Test School", SchoolType.PRIMARY);
        testTeacher = buildTeacher(1L, testSchool, "John", "Doe", "john.doe@school.de");
        createDto = buildCreateDto(1L, "Jane", "Smith", "jane.smith@school.de");
        updateDto = buildUpdateDto("Updated", "Name", "updated@school.de");
        responseDto = buildResponseDto(1L, testSchool, testTeacher);
    }

    @Test
    void getAllReturnsMappedList() {
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
    void getPaginatedDefaultSuccess() {
        Page<Teacher> page = new PageImpl<>(List.of(testTeacher));
        Map<String, String> queryParams = new HashMap<>();

        when(teacherRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        Map<String, Object> resp = teacherService.getPaginated(queryParams, null);

        assertNotNull(resp);
        assertTrue(resp.containsKey("items"));
        assertTrue(resp.containsKey("totalItems"));
        verify(teacherRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getPaginatedWithSearchSuccess() {
        Page<Teacher> page = new PageImpl<>(List.of(testTeacher));
        Map<String, String> queryParams = new HashMap<>();

        when(teacherRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        Map<String, Object> resp = teacherService.getPaginated(queryParams, "John");

        assertNotNull(resp);
        verify(teacherRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getByIdWhenExistsReturnsOptionalDto() {
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(testTeacher));
        when(teacherMapper.toResponseDto(testTeacher)).thenReturn(responseDto);

        Optional<TeacherResponseDto> result = teacherService.getById(1L);

        assertTrue(result.isPresent());
        assertEquals(responseDto, result.get());
        verify(teacherRepository).findById(1L);
        verify(teacherMapper).toResponseDto(testTeacher);
    }

    @Test
    void getByIdWhenNotFoundReturnsEmptyOptional() {
        when(teacherRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<TeacherResponseDto> result = teacherService.getById(99L);

        assertTrue(result.isEmpty());
        verify(teacherRepository).findById(99L);
    }

    @Test
    void createTeacherSuccess() {
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
    void createTeacherDuplicateEmailThrowsException() {
        when(teacherRepository.existsByEmail(createDto.getEmail())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> teacherService.createTeacher(createDto));
        verify(teacherRepository).existsByEmail(createDto.getEmail());
        verify(teacherRepository, never()).save(any());
    }

    @Test
    void createTeacherSchoolNotFoundThrowsException() {
        when(teacherRepository.existsByEmail(createDto.getEmail())).thenReturn(false);
        when(schoolRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> teacherService.createTeacher(createDto));
        verify(teacherRepository).existsByEmail(createDto.getEmail());
        verify(schoolRepository).findById(1L);
        verify(teacherRepository, never()).save(any());
    }

    @Test
    void updateTeacherSuccess() {
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
    void updateTeacherNotFoundThrowsException() {
        when(teacherRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> teacherService.updateTeacher(99L, updateDto));
        verify(teacherRepository).findById(99L);
        verify(teacherRepository, never()).save(any());
    }

    @Test
    void updateTeacherDuplicateEmailThrowsException() {
        updateDto.setEmail("existing@school.de");
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(testTeacher));
        when(teacherRepository.existsByEmailAndIdNot("existing@school.de", 1L)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> teacherService.updateTeacher(1L, updateDto));
        verify(teacherRepository).findById(1L);
        verify(teacherRepository).existsByEmailAndIdNot("existing@school.de", 1L);
        verify(teacherRepository, never()).save(any());
    }

    @Test
    void updateTeacherChangeSchoolSuccess() {
        School newSchool = buildActiveSchool(2L, "New School", SchoolType.PRIMARY);

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
    void deleteTeacherSuccess() {
        when(teacherRepository.existsById(1L)).thenReturn(true);

        teacherService.deleteTeacher(1L);

        verify(teacherRepository).existsById(1L);
        verify(teacherRepository).deleteById(1L);
    }

    @Test
    void deleteTeacherNotFoundThrowsException() {
        when(teacherRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> teacherService.deleteTeacher(99L));
        verify(teacherRepository).existsById(99L);
        verify(teacherRepository, never()).deleteById(anyLong());
    }

    @Test
    void helperMethodsExistAndSortFields() {
        when(teacherRepository.existsById(1L)).thenReturn(true);

        assertTrue(teacherService.existsById(1L));

        List<Map<String, String>> fields = teacherService.getSortFields();
        assertNotNull(fields);
        assertTrue(fields.stream().anyMatch(m -> "id".equals(m.get("key"))));
        assertTrue(fields.stream().anyMatch(m -> "firstName".equals(m.get("key"))));
    }

    private static School buildActiveSchool(Long id, String name, SchoolType type) {
        School school = new School();
        school.setId(id);
        school.setSchoolName(name);
        school.setSchoolType(type);
        school.setZoneNumber(1);
        school.setIsActive(true);
        return school;
    }

    private static Teacher buildTeacher(Long id, School school, String firstName, String lastName, String email) {
        Teacher teacher = new Teacher();
        teacher.setId(id);
        teacher.setSchool(school);
        teacher.setFirstName(firstName);
        teacher.setLastName(lastName);
        teacher.setEmail(email);
        teacher.setPhone("+49841123456");
        teacher.setIsPartTime(false);
        teacher.setEmploymentStatus(EmploymentStatus.ACTIVE);
        teacher.setUsageCycle(UsageCycle.FLEXIBLE);
        return teacher;
    }

    private static TeacherCreateDto buildCreateDto(Long schoolId, String firstName, String lastName, String email) {
        TeacherCreateDto dto = new TeacherCreateDto();
        dto.setSchoolId(schoolId);
        dto.setFirstName(firstName);
        dto.setLastName(lastName);
        dto.setEmail(email);
        dto.setPhone("+49841654321");
        dto.setIsPartTime(false);
        dto.setEmploymentStatus(EmploymentStatus.ACTIVE);
        dto.setUsageCycle(UsageCycle.FLEXIBLE);
        return dto;
    }

    private static TeacherUpdateDto buildUpdateDto(String firstName, String lastName, String email) {
        TeacherUpdateDto dto = new TeacherUpdateDto();
        dto.setFirstName(firstName);
        dto.setLastName(lastName);
        dto.setEmail(email);
        return dto;
    }

    private static TeacherResponseDto buildResponseDto(Long teacherId, School school, Teacher teacher) {
        return TeacherResponseDto.builder()
                .id(teacherId)
                .schoolId(school.getId())
                .schoolName(school.getSchoolName())
                .firstName(teacher.getFirstName())
                .lastName(teacher.getLastName())
                .email(teacher.getEmail())
                .phone(teacher.getPhone())
                .isPartTime(Boolean.FALSE)
                .employmentStatus(EmploymentStatus.ACTIVE)
                .usageCycle(UsageCycle.FLEXIBLE)
                .build();
    }
}
