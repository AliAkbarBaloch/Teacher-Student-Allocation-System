package de.unipassau.allocationsystem.dto;

public class AllocationDto {
    private String id;
    private String studentId;
    private String roomId;
    private String timestamp;

    public AllocationDto() {}

    public AllocationDto(String id, String studentId, String roomId, String timestamp) {
        this.id = id;
        this.studentId = studentId;
        this.roomId = roomId;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}
