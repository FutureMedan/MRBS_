package com.future.MRBS.service;


import com.future.MRBS.model.Room;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface RoomService {
    ResponseEntity createRoom(String roomJSONString, MultipartFile file) throws IOException;

    ResponseEntity updateRoom(String roomJSONString, MultipartFile file) throws IOException;

    ResponseEntity deleteRoom(String RoomId);

    Page<Room> getRooms(String roomName, int page, int size);

    Room getSingleRoom(String id);
}
