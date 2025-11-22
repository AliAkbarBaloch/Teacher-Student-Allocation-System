package de.unipassau.allocationsystem.dto.credittracking;

public class CreditHourTrackingUpdateDto {
    private String notes;
    private Double creditHoursAllocated;
    private Double creditBalance;

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Double getCreditHoursAllocated() {
        return creditHoursAllocated;
    }

    public void setCreditHoursAllocated(Double creditHoursAllocated) {
        this.creditHoursAllocated = creditHoursAllocated;
    }

    public Double getCreditBalance() {
        return creditBalance;
    }

    public void setCreditBalance(Double creditBalance) {
        this.creditBalance = creditBalance;
    }
}
