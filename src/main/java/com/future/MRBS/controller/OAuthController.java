package com.future.MRBS.controller;

import com.future.MRBS.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController @RequestMapping("/oauth") public class OAuthController {
    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private DefaultTokenServices tokenServices;
    private UserService userService;


    @Autowired public OAuthController(DefaultTokenServices tokenServices, UserService userService) {
        this.tokenServices = tokenServices;
        this.userService = userService;
    }

    /**
     * Uncomment for first time run only, endpoint to generate admin account
     * TODO("will delete after development ")
     * @return create a new admin account
     */
  /*  @PostMapping("/admin")
    public ResponseEntity createAdminForFirstTimeAndTestingOnly(){
        User user = User.builder()
                .name("admin")
                .email("admin@jyp.com")
                .password("admin")
                .imageURL("")
                .roles(Arrays.asList("ROLE_ADMIN")).build();
        userService.createUser(user,null);
        return new ResponseEntity(HttpStatus.OK);
    }*/

    @DeleteMapping(value = "/logout", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity revokeToken(Authentication authentication) {
        final String userToken =
            ((OAuth2AuthenticationDetails) authentication.getDetails()).getTokenValue();
        return tokenServices.revokeToken(userToken) ?
            new ResponseEntity(HttpStatus.OK) :
            new ResponseEntity(HttpStatus.BAD_REQUEST);
    }
}
