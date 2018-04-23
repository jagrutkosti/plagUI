package com.plagui.repository;

import com.plagui.modules.docdetails.Timestamps;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TimestampsRepository extends MongoRepository<Timestamps, String> {
    Timestamps findTimestampsByDocHashKey(String docHashKey);
}
