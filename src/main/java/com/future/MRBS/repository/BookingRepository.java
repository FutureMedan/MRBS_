package com.future.MRBS.repository;

import com.future.MRBS.model.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository public interface BookingRepository extends MongoRepository<Booking, String> {
    List<Booking> findAll();

    List<Booking> findByRoomName(String roomName);

    List<Booking> findByRoomNameAndStatusLessThan(String roomName, Integer status);

    List<Booking> findByBookerEmailAndStatusLessThan(String email, Integer status);

    List<Booking> findByStartAtInMillisGreaterThanOrFinishAtInMillisGreaterThanAndStartAtInMillisLessThanAndStatusLessThan(
        Long fromMillis, Long fromMillis2, Long untilMillis, Integer status);

    List<Booking> findByBookerEmailAndStatusLessThanAndStartAtInMillisIsBetweenOrFinishAtInMillisIsBetweenOrStartAtInMillisOrFinishAtInMillis(
        String email, Integer status, Long start, Long finish, Long start2, Long finish2,
        Long start3, Long finish3);

    Page<Booking> findByBookerEmailLikeIgnoreCaseOrRoomNameLikeIgnoreCaseOrMeetingAgendaLikeIgnoreCase(
        String email, String roomName, String meetingAgenda, Pageable pageable);

    Page<Booking> findByBookerEmailAndStatus(String email, Integer status, Pageable pageable);

    Page<Booking> findByBookerEmailAndStatusAndFinishAtInMillisGreaterThan(String email,
        Integer status, Long finishAt, Pageable pageable);

    Booking findBookingById(String id);

    Page<Booking> findByStatusIsLessThanEqualAndFinishAtInMillisGreaterThanEqual(Integer status,
        Long timeInMillis, Pageable pageable);

    Page<Booking> findByRoomNameAndStatusLessThanEqualAndFinishAtInMillisGreaterThan(
        String roomName, Integer status, Long timeInMillis, Pageable pageable);
}
