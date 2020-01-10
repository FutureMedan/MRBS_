package com.future.MRBS.repository;

import com.future.MRBS.model.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository public interface RoomRepository extends MongoRepository<Room, String> {
    Room findRoomById(String id);

    List<Room> findAll();

    Room findByName(String name);

    List<Room> findByNameLikeIgnoreCaseAndCategoryLikeIgnoreCaseAndFacilityLikeIgnoreCase(
        String name, String category, List<String> facility, Sort sort);

    Page<Room> findByNameLikeIgnoreCase(String roomName, Pageable pageable);
}
