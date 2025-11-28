package de.unipassau.allocationsystem.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "plan_change_logs", indexes = {
        @Index(name = "idx_plan_change_plan", columnList = "plan_id"),
        @Index(name = "idx_plan_change_user", columnList = "user_id"),
        @Index(name = "idx_plan_change_entity_type", columnList = "entity_type"),
        @Index(name = "idx_plan_change_timestamp", columnList = "event_timestamp")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanChangeLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false, foreignKey = @ForeignKey(name = "fk_plan_change_plan"))
    private AllocationPlan allocationPlan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_plan_change_user"))
    private User user;

    @Column(name = "change_type", nullable = false, length = 50)
    private String changeType;

    @Column(name = "entity_type", nullable = false, length = 100)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    @Column(name = "event_timestamp", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime eventTimestamp;

    @Column(name = "reason", length = 500)
    private String reason;

    @PrePersist
    protected void onCreate() {
        if (eventTimestamp == null) {
            eventTimestamp = LocalDateTime.now();
        }
    }
}
