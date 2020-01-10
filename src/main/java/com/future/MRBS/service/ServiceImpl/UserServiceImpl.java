package com.future.MRBS.service.ServiceImpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.future.MRBS.Utils.Utils;
import com.future.MRBS.config.custom.services.CustomTokenStore;
import com.future.MRBS.model.Booking;
import com.future.MRBS.model.User;
import com.future.MRBS.repository.BookingRepository;
import com.future.MRBS.repository.UserRepository;
import com.future.MRBS.service.AmazonClientService;
import com.future.MRBS.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static com.future.MRBS.Utils.Utils.createErrorResponse;
import static com.future.MRBS.Utils.Utils.createPageRequest;
import static com.future.MRBS.service.ServiceImpl.BookingServiceImpl.STATUS_CANCELED_OR_DELETED;
import static com.future.MRBS.service.ServiceImpl.BookingServiceImpl.STATUS_CHECKED_OUT;

@Service public class UserServiceImpl implements UserDetailsService, UserService {
    final static String USER_NOT_FOUND = "User not found";
    private final String USER_EXIST = "User already exist";
    private PasswordEncoder passwordEncoder;
    private UserRepository userRepository;
    private AmazonClientService amazonClientService;
    private DefaultTokenServices tokenServices;
    private BookingRepository bookingRepository;
    private CustomTokenStore customTokenStore;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, BookingRepository bookingRepository,
        DefaultTokenServices tokenServices, AmazonClientService amazonClientService,
        CustomTokenStore customTokenStore, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.tokenServices = tokenServices;
        this.customTokenStore = customTokenStore;
        this.passwordEncoder = passwordEncoder;
        this.amazonClientService = amazonClientService;
    }

    @Override public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        final User userExist = userRepository.findByEmail(email);
        if (userExist == null)
            throw new UsernameNotFoundException(String.format("User %s Not Found!", email));

        Collection<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        for (String role : userExist.getRoles()) {
            GrantedAuthority authority = new SimpleGrantedAuthority(role);
            grantedAuthorities.add(authority);
        }
        return new org.springframework.security.core.userdetails.User(userExist.getUsername(),
            userExist.getPassword(), grantedAuthorities);
    }

    @Override public ResponseEntity createUser(String userJSONString, MultipartFile file)
        throws IOException {
        User user = new ObjectMapper().readValue(userJSONString, User.class);
        ResponseEntity response;
        final User userExist = userRepository.findByEmail(user.getEmail());
        if (userExist != null) {
            response = createErrorResponse(USER_EXIST, HttpStatus.INTERNAL_SERVER_ERROR);
        } else {
            if (file != null) {
                user.setImageURL(amazonClientService.uploadFile(file, user.getName()));
            }
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            userRepository.save(user);
            response = new ResponseEntity<>(user, HttpStatus.OK);
        }
        return response;
    }

    @Override public ResponseEntity updateUserProfile(String userJSONString, MultipartFile file,
        Authentication authentication) throws IOException {
        final User user = new ObjectMapper().readValue(userJSONString, User.class);
        ResponseEntity response;
        final User userExist = userRepository.findByEmail(user.getEmail());
        if (userExist == null) {
            response = createErrorResponse(USER_NOT_FOUND, HttpStatus.NOT_FOUND);
        } else {
            userExist.setName(user.getName());
            userExist.setAddress(user.getAddress());
            userExist.setPassword(passwordEncoder.encode(user.getPassword()));
            userExist.setPhoneNumber(user.getPhoneNumber());
            userExist.setRoles(user.getRoles());
            if (file != null) {
                amazonClientService.deleteFileFromS3Bucket(userExist.getImageURL());
                userExist.setImageURL(amazonClientService.uploadFile(file, user.getName()));
            }
            if (!authentication.getName().equalsIgnoreCase(userExist.getEmail())) {
                revokeUserToken(userExist.getEmail());
            }
            userRepository.save(userExist);
            response = new ResponseEntity<>(userExist, HttpStatus.OK);
        }
        return response;
    }

    @Override public ResponseEntity deleteUser(String userId, Authentication authentication) {
        Optional<User> userExist = userRepository.findById(userId);
        ResponseEntity response;
        if (!userExist.isPresent()) {
            response = createErrorResponse(USER_NOT_FOUND, HttpStatus.NOT_FOUND);
        } else {
            final List<Booking> bookingList = bookingRepository
                .findByBookerEmailAndStatusLessThan(userExist.get().getEmail(), STATUS_CHECKED_OUT);
            if (null != bookingList) {
                bookingList.forEach(booking -> booking.setStatus(STATUS_CANCELED_OR_DELETED));
                bookingRepository.saveAll(bookingList);
            }
            revokeUserToken(userExist.get().getEmail());
            amazonClientService.deleteFileFromS3Bucket(userExist.get().getImageURL());
            userRepository.delete(userExist.get());
            response = new ResponseEntity(HttpStatus.OK);
        }
        return response;
    }

    @Override public Page<User> getUsers(String searchKeyWord, int page, int size) {
        Page<User> userPage;
        if (searchKeyWord.trim().isEmpty()) {
            userPage =
                userRepository.findAll(createPageRequest("name", Utils.SORT_ASC, page, size));
        } else {
            userPage = userRepository
                .findByEmailLikeIgnoreCaseOrNameLikeIgnoreCase(searchKeyWord, searchKeyWord,
                    createPageRequest("name", Utils.SORT_ASC, page, size));
        }
        return userPage;
    }

    @Override public ResponseEntity getSingleUser(Authentication authentication) {
        User userExist = userRepository.findByEmail(authentication.getName());
        ResponseEntity response;
        if (userExist == null) {
            response = createErrorResponse(USER_EXIST, HttpStatus.INTERNAL_SERVER_ERROR);
        } else {
            response = new ResponseEntity<>(userExist, HttpStatus.OK);
        }
        return response;
    }

    private User getUserById(String userId) {
        return userRepository.findUserById(userId);
    }

    private void revokeUserToken(String email) {
        final String token = customTokenStore.getTokenValueByUserName(email);
        tokenServices.revokeToken(token);
    }

    @Override public void createAdminUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }
}
