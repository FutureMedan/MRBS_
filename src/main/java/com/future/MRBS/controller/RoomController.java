package com.future.MRBS.controller;

import com.future.MRBS.model.Room;
import com.future.MRBS.service.RoomService;
import com.mongodb.lang.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static com.future.MRBS.controller.BookingController.ROLE_ADMIN;

@RestController @RequestMapping("/api") public class RoomController {
    private RoomService roomService;

    @Autowired RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @Secured({ROLE_ADMIN}) @GetMapping("/room") public Page<Room> getAllRoom(
        @RequestParam(value = "searchKeyWord", required = false, defaultValue = "") String roomLike,
        @RequestParam(value = "page", required = false, defaultValue = "0") int page,
        @RequestParam(value = "size", required = false, defaultValue = "10") int size) {
        return roomService.getRooms(roomLike, page, size);
    }

    @GetMapping("/room/{id}") public Room getSingleRoom(@PathVariable("id") String id) {
        return roomService.getSingleRoom(id);
    }

    @Secured(ROLE_ADMIN) @PostMapping("/room")
    public ResponseEntity createRoom(@Nullable @RequestPart("room") String roomJSONString,
        @Nullable @RequestPart("file") MultipartFile file) throws IOException {
        return roomService.createRoom(roomJSONString, file);
    }

    @Secured(ROLE_ADMIN) @PutMapping("/room")
    public ResponseEntity updateRoom(@Nullable @RequestPart("room") String roomJSONString,
        @Nullable @RequestPart("file") MultipartFile file) throws IOException {
        return roomService.updateRoom(roomJSONString, file);
    }

    @Secured({ROLE_ADMIN}) @DeleteMapping("/room/{roomId}")
    public ResponseEntity deleteRoom(@PathVariable("roomId") String roomId) {
        return roomService.deleteRoom(roomId);
    }
}
