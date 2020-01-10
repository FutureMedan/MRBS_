package com.future.MRBS.service;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

public interface ConfigService {
    ResponseEntity getConfigCode(Authentication authentication);

    ResponseEntity updateConfigCode(String newCode, Authentication authentication);

    ResponseEntity resetConfigCode(Authentication authentication);
}
