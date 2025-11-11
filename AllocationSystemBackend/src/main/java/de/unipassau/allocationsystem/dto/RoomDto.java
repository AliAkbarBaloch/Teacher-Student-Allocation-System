package de.unipassau.allocationsystem.dto;

import java.util.List;

public class RoomDto {
    private String id;
    private int capacity;
    private List<String> features;

    public RoomDto() {}

    public RoomDto(String id, int capacity, List<String> features) {
        this.id = id;
        this.capacity = capacity;
        this.features = features;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public List<String> getFeatures() { return features; }
    public void setFeatures(List<String> features) { this.features = features; }
}
