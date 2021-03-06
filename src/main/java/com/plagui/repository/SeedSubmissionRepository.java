package com.plagui.repository;

import com.plagui.modules.docdetails.SeedSubmission;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

/**
 * Created by Jagrut on 01-06-2017.
 */
public interface SeedSubmissionRepository extends MongoRepository<SeedSubmission, String> {
    @Query(value = "{'plagchain_seed': {$regex : ?0}}")
    SeedSubmission findByOriginstampSeedRegex(String regexString);
}
