package com.future.MRBS.controller;

import com.future.MRBS.model.Booking;
import com.future.MRBS.model.CheckIn;
import com.future.MRBS.model.CheckOut;
import com.future.MRBS.model.Room;
import com.future.MRBS.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api") public class BookingController {

    static final String ROLE_ADMIN = "ROLE_ADMIN";
    private BookingService bookingService;

    @Autowired BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/booking")
    public ResponseEntity createBooking(@RequestBody Booking booking, Authentication auth) {
        return bookingService.createBooking(booking, auth);
    }

    @Secured({ROLE_ADMIN}) @GetMapping("/booking") public Page<Booking> getAllBooking(
        @RequestParam(value = "searchKeyWord", required = false, defaultValue = "")
            String searchKeyWord,
        @RequestParam(value = "oncoming", required = false, defaultValue = "0") Boolean onComing,
        @RequestParam(value = "page", required = false, defaultValue = "0") int page,
        @RequestParam(value = "size", required = false, defaultValue = "10") int size) {
        return bookingService.getBookings(searchKeyWord, onComing, page, size);
    }

    @GetMapping("/booking/{roomName}")
    public Page<Booking> getBookingByRoomNameAndStatus(@PathVariable("roomName") String roomName,
        @RequestParam(value = "page", required = false, defaultValue = "0") int page,
        @RequestParam(value = "size", required = false, defaultValue = "10") int size) {
        return bookingService.getBookingByRoomNameAndStatus(roomName, page, size);
    }

    @GetMapping("/booking/mybooking")
    public Page<Booking> getMyBooking(Authentication authentication,
        @RequestParam(value = "status", required = false, defaultValue = "0") int status,
        @RequestParam(value = "page", required = false, defaultValue = "0") int page,
        @RequestParam(value = "size", required = false, defaultValue = "10") int size) {
        return bookingService.getMyBooking(authentication, status, page, size);
    }

    @GetMapping("/booking/availablerooms")
    public ResponseEntity<Page<Room>> getAvailableRooms(Authentication authentication,
        @RequestParam("from") Long fromMillis, @RequestParam("until") Long untilMillis,
        @RequestParam(value = "room_name", required = false, defaultValue = "") String roomName,
        @RequestParam(value = "category", required = false, defaultValue = "") String category,
        @RequestParam(value = "facility") String facility,
        @RequestParam(value = "page", required = false, defaultValue = "0") int page,
        @RequestParam(value = "size", required = false, defaultValue = "10") int size) {
        return bookingService
            .getAvailableRooms(fromMillis, untilMillis, roomName, category, facility, page, size,
                authentication);
    }

    @PutMapping("/booking/checkin")
    public ResponseEntity checkInBooking(@RequestBody CheckIn checkIn) {
        return bookingService.checkInBooking(checkIn.getBookingId(), checkIn.getOtp());
    }

    @PutMapping("/booking/checkout")
    public ResponseEntity checkOutBooking(@RequestBody CheckOut checkOut) {
        return bookingService.checkOutBooking(checkOut.getBookingId());
    }

    @DeleteMapping("/booking/{bookingId}")
    public ResponseEntity deleteBooking(@PathVariable("bookingId") String bookingId) {
        return bookingService.cancelBooking(bookingId);
    }
}
