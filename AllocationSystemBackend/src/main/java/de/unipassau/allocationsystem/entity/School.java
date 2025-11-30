package de.unipassau.allocationsystem.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing a school participating in the allocation system.
 * Schools are the primary locations where teachers are assigned for internships.
 */
@Entity
@Table(name = "schools", indexes = {
        @Index(name = "idx_school_type", columnList = "school_type"),
        @Index(name = "idx_zone_number", columnList = "zone_number"),
        @Index(name = "idx_is_active", columnList = "is_active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class School {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "School name is required")
    @Size(min = 3, message = "School name must be at least 3 characters")
    @Column(name = "school_name", nullable = false, unique = true)
    private String schoolName;

    @NotNull(message = "School type is required")
    @Column(name = "school_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private SchoolType schoolType;

    @NotNull(message = "Zone number is required")
    @Positive(message = "Zone number must be positive")
    @Column(name = "zone_number", nullable = false)
    private Integer zoneNumber;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "latitude", precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(name = "distance_from_center", precision = 10, scale = 2)
    private BigDecimal distanceFromCenter;

    @Column(name = "transport_accessibility", length = 255)
    private String transportAccessibility;

    @Email(message = "Contact email must be valid")
    @Column(name = "contact_email")
    private String contactEmail;

    @Pattern(regexp = "^[+]?[(]?[0-9]{1,4}[)]?[-\\s./0-9]*$", 
             message = "Contact phone must be a valid phone number")
    @Column(name = "contact_phone")
    private String contactPhone;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.isActive == null) {
            this.isActive = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Enum representing the types of schools in the system.
     */
    public enum SchoolType {
        PRIMARY,
        MIDDLE,
        SECONDARY,
        VOCATIONAL,
        SPECIAL_EDUCATION
    }
}
