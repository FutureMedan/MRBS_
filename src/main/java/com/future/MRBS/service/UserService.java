package com.future.MRBS.service;


import com.future.MRBS.model.User;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface UserService {
    ResponseEntity createUser(String userJSONString, MultipartFile file) throws IOException;

    ResponseEntity updateUserProfile(String userJSONString, MultipartFile file,
        Authentication authentication) throws IOException;

    ResponseEntity deleteUser(String userId, Authentication authentication);

    Page<User> getUsers(String searchKeyWord, int page, int size);

    ResponseEntity getSingleUser(Authentication authentication);

    void createAdminUser(User user);
}
