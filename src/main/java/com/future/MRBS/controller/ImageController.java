package com.future.MRBS.controller;

import com.future.MRBS.Utils.Utils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController @RequestMapping("/assets") public class ImageController {
    @GetMapping(value = "/{type}/{imageName:.+}")
    public ResponseEntity getPaymentImage(@PathVariable("type") String type,
        @PathVariable("imageName") String imageName) {
        return Utils.getImage(type + "/" + imageName);
    }
}
