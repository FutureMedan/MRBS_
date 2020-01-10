package com.future.MRBS.repository;

import com.future.MRBS.model.Config;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository public interface ConfigRepository extends MongoRepository<Config, String> {
    Config findConfigCodeById(String id);
}
