package com.future.MRBS.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "room")
public class Room {
    private String id;
    private String name;
    private String location;
    private String category;
    private String imageURL;
    private List<String> facility;
    private Integer capacity;
}
