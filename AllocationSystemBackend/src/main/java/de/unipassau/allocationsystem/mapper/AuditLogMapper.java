package de.unipassau.allocationsystem.mapper;

import de.unipassau.allocationsystem.dto.auditlog.AuditLogDto;
import de.unipassau.allocationsystem.entity.AuditLog;
import de.unipassau.allocationsystem.entity.User;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
/**
 * Mapper for converting between AuditLog entities and DTOs.
 * Handles audit log transformations with user resolution.
 */
public class AuditLogMapper {

    private final UserRepository userRepository;

    /**
     * Converts an audit log DTO to an entity.
     * 
     * @param dto the audit log DTO
     * @return audit log entity
     * @throws ResourceNotFoundException if user not found
     */
    public AuditLog toEntity(AuditLogDto dto) {
        if (dto == null) {
            return null;
        }

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + dto.getUserId()));

        AuditLog.AuditLogBuilder builder = AuditLog.builder()
                .user(user)
                .userIdentifier(dto.getUserIdentifier())
                .eventTimestamp(dto.getEventTimestamp())
                .action(dto.getAction())
                .targetEntity(dto.getTargetEntity())
                .targetRecordId(dto.getTargetRecordId())
                .previousValue(dto.getPreviousValue())
                .newValue(dto.getNewValue())
                .description(dto.getDescription())
                .ipAddress(dto.getIpAddress())
                .createdAt(dto.getCreatedAt());

        if (dto.getId() != null && dto.getId() > 0) {
            builder.id(dto.getId());
        }

        return builder.build();
    }

    /**
     * Converts an audit log entity to a DTO.
     * 
     * @param entity the audit log entity
     * @return audit log DTO
     */
    public AuditLogDto toDto(AuditLog entity) {
        if (entity == null) {
            return null;
        }
        return AuditLogDto.builder()
                .id(entity.getId())
                .userId(Optional.ofNullable(entity.getUser()).map(User::getId).orElse(0L))
                .userIdentifier(entity.getUserIdentifier())
                .eventTimestamp(entity.getEventTimestamp())
                .action(entity.getAction())
                .targetEntity(entity.getTargetEntity())
                .targetRecordId(entity.getTargetRecordId())
                .previousValue(entity.getPreviousValue())
                .newValue(entity.getNewValue())
                .description(entity.getDescription())
                .ipAddress(entity.getIpAddress())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    /**
     * Converts a list of audit log entities to DTOs.
     * 
     * @param entities list of audit log entities
     * @return list of audit log DTOs
     */
    public List<AuditLogDto> toDtoList(List<AuditLog> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
