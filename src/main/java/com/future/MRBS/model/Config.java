package com.future.MRBS.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data @Builder @AllArgsConstructor @NoArgsConstructor @Document(collection = "Config")
public class Config {
    @Id private String id;
    private String code;
    private String lastEditBy;
    private Long lastEditAt;
}
