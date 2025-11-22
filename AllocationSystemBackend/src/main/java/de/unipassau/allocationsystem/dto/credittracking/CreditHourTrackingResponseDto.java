package de.unipassau.allocationsystem.dto.credittracking;

import java.time.LocalDateTime;

public class CreditHourTrackingResponseDto {
    private Long id;
    private Long teacherId;
    private Long yearId;
    private Integer assignmentsCount;
    private Double creditHoursAllocated;
    private Double creditBalance;
    private String notes;
    private LocalDateTime lastUpdated;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
    public Long getYearId() { return yearId; }
    public void setYearId(Long yearId) { this.yearId = yearId; }
    public Integer getAssignmentsCount() { return assignmentsCount; }
    public void setAssignmentsCount(Integer assignmentsCount) { this.assignmentsCount = assignmentsCount; }
    public Double getCreditHoursAllocated() { return creditHoursAllocated; }
    public void setCreditHoursAllocated(Double creditHoursAllocated) { this.creditHoursAllocated = creditHoursAllocated; }
    public Double getCreditBalance() { return creditBalance; }
    public void setCreditBalance(Double creditBalance) { this.creditBalance = creditBalance; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
}
