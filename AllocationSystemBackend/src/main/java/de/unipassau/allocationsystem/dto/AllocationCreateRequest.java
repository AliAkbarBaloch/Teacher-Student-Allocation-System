package de.unipassau.allocationsystem.dto;

public class AllocationCreateRequest {
    private String studentId;
    private String roomId;

    public AllocationCreateRequest() {}

    public AllocationCreateRequest(String studentId, String roomId) {
        this.studentId = studentId;
        this.roomId = roomId;
    }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
}
