package com.future.MRBS.repository;

import com.future.MRBS.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository public interface UserRepository extends MongoRepository<User, String> {
    User findUserById(String Id);

    List<User> findAll();

    User findByEmail(String email);

    Page<User> findByEmailLikeIgnoreCaseOrNameLikeIgnoreCase(String email, String name,
        Pageable pageable);
}
