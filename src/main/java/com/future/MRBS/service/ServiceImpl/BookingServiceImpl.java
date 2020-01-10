package com.future.MRBS.service.ServiceImpl;

import com.future.MRBS.Utils.Utils;
import com.future.MRBS.model.Booking;
import com.future.MRBS.model.Room;
import com.future.MRBS.model.User;
import com.future.MRBS.repository.BookingRepository;
import com.future.MRBS.repository.RoomRepository;
import com.future.MRBS.repository.UserRepository;
import com.future.MRBS.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static com.future.MRBS.Utils.Utils.*;
import static com.future.MRBS.service.ServiceImpl.UserServiceImpl.USER_NOT_FOUND;

@Service public class BookingServiceImpl implements BookingService {
    final static int STATUS_BOOKED = 0;
    final static int STATUS_CHECKED_IN = 1;
    final static int STATUS_CHECKED_OUT = 2;
    final static int STATUS_CANCELED_OR_DELETED = 3;
    private static final String INVALID_OTP = "Invalid OTP";
    private static final String BOOKING_NOT_FOUND = "Booking not found";
    private static final String INVALID_BOOKING_STATUS = "Invalid booking status";
    private static final String ROOM_IS_UNAVAILABLE = "Room is unavailable";
    private BookingRepository bookingRepository;
    private RoomRepository roomRepository;
    private UserRepository userRepository;

    @Autowired
    public BookingServiceImpl(BookingRepository bookingRepository, RoomRepository roomRepository,
        UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
    }

    private String getRandomNumberString() {
        Random rnd = new Random();
        int number = rnd.nextInt(999999);
        return String.format("%06d", number);
    }

    private Boolean isUnavailableRoom(Room roomExist, Booking booking) {
        List<Room> roomList = roomRepository.
            findByNameLikeIgnoreCaseAndCategoryLikeIgnoreCaseAndFacilityLikeIgnoreCase(
                roomExist.getName(), roomExist.getCategory(), roomExist.getFacility(),
                Sort.by(Sort.Direction.ASC, "name"));
        List<Booking> bookingList = bookingRepository.
            findByStartAtInMillisGreaterThanOrFinishAtInMillisGreaterThanAndStartAtInMillisLessThanAndStatusLessThan(
                booking.getStartAtInMillis(), booking.getStartAtInMillis(),
                booking.getFinishAtInMillis(), STATUS_CHECKED_OUT);
        List<Room> unavailableRooms =
            findUnavailableRooms(booking.getStartAtInMillis(), booking.getFinishAtInMillis(),
                roomList, bookingList);
        return unavailableRooms.contains(roomExist);
    }

    @Override public ResponseEntity createBooking(Booking booking, Authentication authentication) {
        final Room roomExist = roomRepository.findRoomById(booking.getRoom().getId());
        final String bookerEmail = authentication.getName();
        final User userExist = userRepository.findByEmail(bookerEmail);
        ResponseEntity response;
        if (userExist == null) {
            response = createErrorResponse(USER_NOT_FOUND, HttpStatus.NOT_FOUND);
        } else if (roomExist == null) {
            response = createErrorResponse(BOOKING_NOT_FOUND, HttpStatus.NOT_FOUND);
        } else {
            if (isUnavailableRoom(roomExist, booking)) {
                response = createErrorResponse(ROOM_IS_UNAVAILABLE, HttpStatus.BAD_REQUEST);
            } else {
                booking.setBookerEmail(bookerEmail);
                booking.setRoom(roomExist);
                booking.setOtp(getRandomNumberString());
                booking.setStatus(STATUS_BOOKED);
                booking.setCreatedDate(System.currentTimeMillis());
                Booking bookingCreated = bookingRepository.save(booking);
                return new ResponseEntity<>(bookingCreated, HttpStatus.OK);
            }
        }
        return response;
    }

    @Override public ResponseEntity cancelBooking(String bookingId) {
        final Booking bookingExist = bookingRepository.findBookingById(bookingId);
        ResponseEntity response;
        if (bookingExist == null) {
            response = createErrorResponse(BOOKING_NOT_FOUND, HttpStatus.NOT_FOUND);
        } else {
            if (bookingExist.getStatus() == STATUS_CHECKED_IN) {
                bookingExist.setStatus(STATUS_CHECKED_OUT);
            } else {
                bookingExist.setStatus(STATUS_CANCELED_OR_DELETED);
            }
            bookingRepository.save(bookingExist);
            response = new ResponseEntity(HttpStatus.OK);
        }
        return response;
    }

    @Override
    public Page<Booking> getBookings(String searchKeyWord, Boolean onComing, int page, int size) {
        Page<Booking> bookingPage;
        if (onComing) {
            bookingPage = getOnComingAndOnGoingBooking(page, size);
        } else {
            if (searchKeyWord.trim().isEmpty()) {
                bookingPage = bookingRepository
                    .findAll(createPageRequest("startAtInMillis", Utils.SORT_ASC, page, size));
            } else {
                bookingPage = bookingRepository
                    .findByBookerEmailLikeIgnoreCaseOrRoomNameLikeIgnoreCaseOrMeetingAgendaLikeIgnoreCase(
                        searchKeyWord, searchKeyWord, searchKeyWord,
                        createPageRequest("startAtInMillis", Utils.SORT_ASC, page, size));
            }
        }
        return bookingPage;
    }

    @Override public ResponseEntity checkInBooking(String bookingId, String otp) {
        Booking bookingExist = bookingRepository.findBookingById(bookingId);
        ResponseEntity response;
        if (bookingExist == null) {
            response = createErrorResponse(BOOKING_NOT_FOUND, HttpStatus.NOT_FOUND);
        } else if (bookingExist.getOtp().equalsIgnoreCase(otp)
            && System.currentTimeMillis() < bookingExist.getFinishAtInMillis()) {
            bookingExist.setStatus(STATUS_CHECKED_IN);
            bookingExist.setCheckinAtMillis(System.currentTimeMillis());
            bookingRepository.save(bookingExist);
            response = new ResponseEntity<>(bookingExist, HttpStatus.OK);
        } else {
            response = createErrorResponse(INVALID_OTP, HttpStatus.BAD_REQUEST);
        }
        return response;
    }

    @Override public ResponseEntity checkOutBooking(String bookingId) {
        Booking bookingExist = bookingRepository.findBookingById(bookingId);
        ResponseEntity response;
        if (bookingExist == null) {
            response = createErrorResponse(BOOKING_NOT_FOUND, HttpStatus.NOT_FOUND);
        } else if (bookingExist.getStatus() != STATUS_CHECKED_IN) {
            response = createErrorResponse(INVALID_BOOKING_STATUS, HttpStatus.ALREADY_REPORTED);
        } else {
            bookingExist.setStatus(STATUS_CHECKED_OUT);
            bookingExist.setCheckoutAtInMillis(System.currentTimeMillis());
            bookingRepository.save(bookingExist);
            response = new ResponseEntity<>(bookingExist, HttpStatus.OK);
        }
        return response;
    }

    @Override public Page<Booking> getMyBooking(Authentication authentication, int status, int page,
        int size) {
        final String bookerEmail = authentication.getName();
        return status == STATUS_BOOKED ?
            bookingRepository
                .findByBookerEmailAndStatusAndFinishAtInMillisGreaterThan(bookerEmail, status,
                    System.currentTimeMillis(),
                    createPageRequest("startAtInMillis", Utils.SORT_ASC, page, size)) :
            bookingRepository.findByBookerEmailAndStatus(bookerEmail, status,
                createPageRequest("startAtInMillis", Utils.SORT_ASC, page, size));
    }

    @Override public ResponseEntity<Page<Room>> getAvailableRooms(Long fromMillis, Long untilMillis,
        String roomName, String category, String facility, int page, int size,
        Authentication authentication) {
        ResponseEntity<Page<Room>> response = new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        if (getBookingEligibility(authentication.getName(), fromMillis, untilMillis)) {
            List<String> facilities = Arrays.asList(facility.split("\\s*,\\s*"));
            List<Room> roomList = roomRepository.
                findByNameLikeIgnoreCaseAndCategoryLikeIgnoreCaseAndFacilityLikeIgnoreCase(roomName,
                    category, facilities, Sort.by(Sort.Direction.ASC, "name"));
            List<Booking> bookingList = bookingRepository.
                findByStartAtInMillisGreaterThanOrFinishAtInMillisGreaterThanAndStartAtInMillisLessThanAndStatusLessThan(
                    fromMillis, fromMillis, untilMillis, STATUS_CHECKED_OUT);
            roomList
                .removeAll(findUnavailableRooms(fromMillis, untilMillis, roomList, bookingList));
            final Pageable pageable = createPageRequest("name", SORT_ASC, page, size);
            final int start = (int) pageable.getOffset();
            final int end = Math.min((start + pageable.getPageSize()), roomList.size());
            List<Room> pagedRoom = roomList.subList(start, end);
            response =
                new ResponseEntity<Page<Room>>(new PageImpl<>(pagedRoom, pageable, roomList.size()),
                    HttpStatus.OK);
        }
        return response;
    }

    private List<Room> findUnavailableRooms(Long fromMillis, Long untilMillis, List<Room> roomList,
        List<Booking> bookingList) {
        List<Room> unavailableRooms = new ArrayList<>();
        for (Room room : roomList) {
            for (Booking booking : bookingList) {
                if ((room.getId().equalsIgnoreCase(booking.getRoom().getId()))) {
                    if ((booking.getStartAtInMillis() > fromMillis
                        || booking.getFinishAtInMillis() > fromMillis) && (
                        booking.getStartAtInMillis() < untilMillis)) {
                        unavailableRooms.add(room);
                        break;
                    }
                }
            }
        }
        return unavailableRooms;
    }

    private Page<Booking> getOnComingAndOnGoingBooking(int page, int size) {
        return bookingRepository
            .findByStatusIsLessThanEqualAndFinishAtInMillisGreaterThanEqual(STATUS_CHECKED_IN,
                System.currentTimeMillis(),
                createPageRequest("startAtInMillis", SORT_ASC, page, size));
    }

    private boolean getBookingEligibility(String userEmail, Long startTime, Long finishTime) {
        List<Booking> bookingList = bookingRepository
            .findByBookerEmailAndStatusLessThanAndStartAtInMillisIsBetweenOrFinishAtInMillisIsBetweenOrStartAtInMillisOrFinishAtInMillis(
                userEmail, STATUS_CHECKED_OUT, startTime, finishTime, startTime, finishTime,
                startTime, finishTime);
        // TODO ("Have to refactor the query! the query result still return bookings that does not belong to userEmail,
        //  codes below is only for temporary fix")
        bookingList = bookingList.stream().filter(
            booking -> booking.getBookerEmail().equalsIgnoreCase(userEmail)
                && booking.getStatus() < STATUS_CHECKED_OUT).collect(Collectors.toList());
        return bookingList.isEmpty();
    }

    @Override
    public Page<Booking> getBookingByRoomNameAndStatus(String roomName, int page, int size) {
        return bookingRepository
            .findByRoomNameAndStatusLessThanEqualAndFinishAtInMillisGreaterThan(roomName,
                STATUS_BOOKED, System.currentTimeMillis(),
                createPageRequest("startAtInMillis", SORT_ASC, page, size));
    }
}
