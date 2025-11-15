package de.unipassau.allocationsystem.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity representing audit log entries for tracking user actions and system changes.
 * Provides traceability, compliance support, and historical review capabilities.
 */
@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_user", columnList = "user_id"),
    @Index(name = "idx_audit_entity", columnList = "target_entity"),
    @Index(name = "idx_audit_action", columnList = "action"),
    @Index(name = "idx_audit_timestamp", columnList = "event_timestamp")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    private static final int USER_IDENTIFIER_LENGTH = 255;
    private static final int ACTION_LENGTH = 50;
    private static final int ENTITY_NAME_LENGTH = 100;
    private static final int RECORD_ID_LENGTH = 100;
    private static final int DESCRIPTION_LENGTH = 500;
    private static final int IP_ADDRESS_LENGTH = 45;
    private static final int USER_AGENT_LENGTH = 500;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Reference to the user who performed the action.
     * Foreign key constraint is disabled to allow audit logs to persist even after user deletion
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private User user;

    /**
     * Email or identifier of the user (denormalized for performance and data retention).
     */
    @Column(name = "user_identifier", nullable = false, length = USER_IDENTIFIER_LENGTH)
    private String userIdentifier;

    /**
     * Timestamp when the event occurred.
     */
    @Column(name = "event_timestamp", nullable = false)
    private LocalDateTime eventTimestamp;

    /**
     * Type of action performed (CREATE, UPDATE, DELETE, VIEW, LOGIN, LOGOUT, etc.).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = ACTION_LENGTH)
    private AuditAction action;

    /**
     * Name of the target entity/table (e.g., "User", "Role", "AllocationPlan").
     */
    @Column(name = "target_entity", nullable = false, length = ENTITY_NAME_LENGTH)
    private String targetEntity;

    /**
     * ID of the specific record that was affected.
     */
    @Column(name = "target_record_id", length = RECORD_ID_LENGTH)
    private String targetRecordId;

    /**
     * Previous value before the change (JSON format for complex objects).
     */
    @Column(name = "previous_value", columnDefinition = "TEXT")
    private String previousValue;

    /**
     * New value after the change (JSON format for complex objects).
     */
    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    /**
     * Additional context or metadata about the action.
     */
    @Column(name = "description", length = DESCRIPTION_LENGTH)
    private String description;

    /**
     * IP address of the user performing the action.
     */
    @Column(name = "ip_address", length = IP_ADDRESS_LENGTH)
    private String ipAddress;

    /**
     * User agent string from the request.
     */
    @Column(name = "user_agent", length = USER_AGENT_LENGTH)
    private String userAgent;

    /**
     * Timestamp when this audit record was created (usually same as eventTimestamp).
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (eventTimestamp == null) {
            eventTimestamp = LocalDateTime.now();
        }
    }

    /**
     * Enum representing possible audit actions.
     */
    public enum AuditAction {
        CREATE,
        UPDATE,
        DELETE,
        VIEW,
        LOGIN,
        LOGOUT,
        LOGIN_FAILED,
        PASSWORD_CHANGE,
        PASSWORD_RESET_REQUESTED,
        PASSWORD_RESET,
        PASSWORD_CHANGE_FAILED,
        ACCOUNT_LOCKED,
        PROFILE_UPDATED,
        PERMISSION_CHANGE,
        ROLE_ASSIGNMENT,
        PLAN_CREATED,
        PLAN_MODIFIED,
        PLAN_DELETED,
        ALLOCATION_ASSIGNED,
        ALLOCATION_MODIFIED,
        EXPORT,
        IMPORT,
        SYSTEM_CONFIG_CHANGE
    }
}
