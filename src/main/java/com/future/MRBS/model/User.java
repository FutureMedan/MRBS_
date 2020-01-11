package com.future.MRBS.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data @Builder @AllArgsConstructor @NoArgsConstructor @Document(collection = "user")
public class User {
    @Id private String id;
    private String name;
    private String email;
    private String address;
    private String phoneNumber;
    private String imageURL;
    private String thumbnailURL;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) private String password;
    private List<String> roles;

    public String getImageURL() {
        return imageURL == null ? "" : imageURL;
    }

    public String getThumbnailURL() {
        return thumbnailURL == null ? "" : thumbnailURL;
    }

    @JsonIgnore public String getUsername() {
        return email;
    }
}
