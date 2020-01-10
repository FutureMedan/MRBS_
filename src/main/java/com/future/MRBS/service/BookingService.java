package com.future.MRBS.service;


import com.future.MRBS.model.Booking;
import com.future.MRBS.model.Room;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

public interface BookingService {
    Page<Booking> getBookings(String searchKeyWord, Boolean onComing, int page, int size);

    ResponseEntity checkInBooking(String bookingId, String otp);

    ResponseEntity checkOutBooking(String bookingId);

    Page<Booking> getMyBooking(Authentication authentication, int status, int page, int size);

    ResponseEntity createBooking(Booking booking, Authentication authentication);

    ResponseEntity cancelBooking(String bookingId);

    ResponseEntity<Page<Room>> getAvailableRooms(Long fromMillis, Long untilMillis, String roomName,
        String category, String facility, int page, int size, Authentication authentication);

    Page<Booking> getBookingByRoomNameAndStatus(String roomName, int page, int size);
}
