package com.future.MRBS.controller;

import com.future.MRBS.model.Config;
import com.future.MRBS.service.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import static com.future.MRBS.controller.BookingController.ROLE_ADMIN;

@RestController @RequestMapping("/api") public class ConfigController {
    private ConfigService configService;

    @Autowired public ConfigController(ConfigService configService) {
        this.configService = configService;
    }

    @GetMapping("/config") public ResponseEntity getConfig(Authentication authentication) {
        return configService.getConfigCode(authentication);
    }

    @Secured({ROLE_ADMIN}) @PutMapping("/config")
    public ResponseEntity updateConfig(@RequestBody Config config, Authentication authentication) {
        return configService.updateConfigCode(config.getCode(), authentication);
    }

    @Secured({ROLE_ADMIN}) @PutMapping("/config/reset")
    public ResponseEntity resetConfig(Authentication authentication) {
        return configService.resetConfigCode(authentication);
    }
}
