package com.future.MRBS.controller;

import com.future.MRBS.model.User;
import com.future.MRBS.service.UserService;
import com.mongodb.lang.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static com.future.MRBS.controller.BookingController.ROLE_ADMIN;

@RestController @RequestMapping("/api") public class UserController {
    private UserService userService;

    @Autowired UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/user/myprofile")
    public ResponseEntity getMyProfile(Authentication authentication) {
        return userService.getSingleUser(authentication);
    }

    @Secured({ROLE_ADMIN}) @GetMapping("/user") public Page<User> getAllUser(
        @RequestParam(value = "searchKeyWord", required = false, defaultValue = "")
            String searchKeyWord,
        @RequestParam(value = "page", required = false, defaultValue = "0") int page,
        @RequestParam(value = "size", required = false, defaultValue = "10") int size) {
        return userService.getUsers(searchKeyWord, page, size);
    }

    @Secured({ROLE_ADMIN}) @PostMapping("/user")
    public ResponseEntity createUser(@RequestPart("user") String userJSONString,
        @Nullable @RequestPart("file") MultipartFile file) throws IOException {
        return userService.createUser(userJSONString, file);
    }

    @PutMapping("/user")
    public ResponseEntity updateUser(@RequestPart("user") String userJSONString,
        @Nullable @RequestPart("file") MultipartFile file, Authentication authentication)
        throws IOException {
        return userService.updateUserProfile(userJSONString, file, authentication);
    }

    @Secured({ROLE_ADMIN}) @DeleteMapping("/user/{userId}")
    public ResponseEntity deleteUser(@PathVariable("userId") String userId,
        Authentication authentication) {
        return userService.deleteUser(userId, authentication);
    }
}
