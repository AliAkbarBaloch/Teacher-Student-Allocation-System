package de.unipassau.allocationsystem.controller;

import de.unipassau.allocationsystem.dto.RoomDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/v1/rooms")
public class RoomController {

    @GetMapping
    public ResponseEntity<List<RoomDto>> listRooms() {
        List<RoomDto> rooms = Arrays.asList(
                new RoomDto("room-A", 2, Arrays.asList("projector", "whiteboard")),
                new RoomDto("room-B", 1, Arrays.asList("desk"))
        );
        return ResponseEntity.ok(rooms);
    }
}
