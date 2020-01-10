package com.future.MRBS.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Data @Builder @AllArgsConstructor @NoArgsConstructor @Document(collection = "booking")
public class Booking {
    private String id;
    private String bookerEmail;
    private Room room;
    private String meetingAgenda;
    private String otp;
    private Integer status;
    private Long createdDate;
    private Long startAtInMillis;
    private Long finishAtInMillis;
    private Long checkinAtMillis;
    private Long checkoutAtInMillis;
}
