package com.future.MRBS.service.ServiceImpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.future.MRBS.Utils.Utils;
import com.future.MRBS.model.Booking;
import com.future.MRBS.model.Room;
import com.future.MRBS.repository.BookingRepository;
import com.future.MRBS.repository.RoomRepository;
import com.future.MRBS.service.AmazonClientService;
import com.future.MRBS.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static com.future.MRBS.Utils.Utils.createErrorResponse;
import static com.future.MRBS.Utils.Utils.createPageRequest;
import static com.future.MRBS.service.ServiceImpl.BookingServiceImpl.STATUS_CANCELED_OR_DELETED;
import static com.future.MRBS.service.ServiceImpl.BookingServiceImpl.STATUS_CHECKED_OUT;

@Service public class RoomServiceImpl implements RoomService {
    public static final String MESSAGE = "message";
    private static final String ROOM_EXIST = "Room already exist";
    private static final String ROOM_NOT_FOUND = "Room not found";
    private RoomRepository roomRepository;
    private BookingRepository bookingRepository;
    private AmazonClientService amazonClientService;

    @Autowired
    public RoomServiceImpl(RoomRepository roomRepository, BookingRepository bookingRepository,
        AmazonClientService amazonClientService) {
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
        this.amazonClientService = amazonClientService;
    }

    @Override public ResponseEntity createRoom(String roomJSONString, MultipartFile file)
        throws IOException {
        Room room = new ObjectMapper().readValue(roomJSONString, Room.class);
        ResponseEntity response;
        final Room roomExist = roomRepository.findByName(room.getName());
        if (roomExist != null) {
            response = createErrorResponse(ROOM_EXIST, HttpStatus.INTERNAL_SERVER_ERROR);
        } else {
            if (file != null) {
                room.setImageURL(amazonClientService.uploadFile(file, room.getName()));
            }
            roomRepository.save(room);
            response = new ResponseEntity<>(room, HttpStatus.OK);
        }
        return response;
    }

    @Override public ResponseEntity updateRoom(String roomJSONString, MultipartFile file)
        throws IOException {
        Room room = new ObjectMapper().readValue(roomJSONString, Room.class);
        ResponseEntity response;
        Room roomExist = roomRepository.findRoomById(room.getId());
        if (roomExist == null) {
            response = createErrorResponse(ROOM_NOT_FOUND, HttpStatus.NOT_FOUND);
        } else {
            if (isNewRoomName(room.getName(), roomExist.getName())) {
                final List<Booking> bookingList =
                    bookingRepository.findByRoomName(roomExist.getName());
                if (null != bookingList) {
                    bookingList.forEach(booking -> booking.getRoom().setName(room.getName()));
                    bookingRepository.saveAll(bookingList);
                }
                roomExist.setName(room.getName());
            }
            roomExist.setFacility(room.getFacility());
            roomExist.setCapacity(room.getCapacity());
            roomExist.setCategory(room.getCategory());
            if (file != null) {
                amazonClientService.deleteFileFromS3Bucket(roomExist.getImageURL());
                roomExist.setImageURL(amazonClientService.uploadFile(file, room.getName()));
            }
            roomRepository.save(roomExist);
            response = new ResponseEntity<>(roomExist, HttpStatus.OK);
        }
        return response;
    }

    private Boolean isNewRoomName(String newName, String oldName) {
        return newName.equalsIgnoreCase(oldName) ? false : isRoomNameAvailable(newName);
    }

    private Boolean isRoomNameAvailable(String roomName) {
        return roomRepository.findByName(roomName) == null;
    }

    @Override public ResponseEntity deleteRoom(String roomId) {
        ResponseEntity response;
        Room roomExist = roomRepository.findRoomById(roomId);
        if (roomExist == null) {
            response = createErrorResponse(ROOM_NOT_FOUND, HttpStatus.NOT_FOUND);
        } else {
            final List<Booking> bookingList = bookingRepository
                .findByRoomNameAndStatusLessThan(roomExist.getName(), STATUS_CHECKED_OUT);
            if (null != bookingList) {
                bookingList.forEach(booking -> booking.setStatus(STATUS_CANCELED_OR_DELETED));
                bookingRepository.saveAll(bookingList);
            }
            amazonClientService.deleteFileFromS3Bucket(roomExist.getImageURL());
            roomRepository.delete(roomExist);
            response = new ResponseEntity<>(HttpStatus.OK);
        }
        return response;
    }

    @Override public Page<Room> getRooms(String roomName, int page, int size) {
        Page<Room> roomPage;
        if (roomName.trim().isEmpty()) {
            roomPage =
                roomRepository.findAll(createPageRequest("name", Utils.SORT_ASC, page, size));
        } else {
            roomPage = roomRepository.findByNameLikeIgnoreCase(roomName,
                createPageRequest("name", Utils.SORT_ASC, page, size));
        }
        return roomPage;
    }

    @Override public Room getSingleRoom(String roomId) {
        return roomRepository.findRoomById(roomId);
    }
}
