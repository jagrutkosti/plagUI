package com.plagui.repository;

import com.plagui.modules.plagcheckrequests.PlagCheckRequests;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

/**
 * Created by Jagrut on 03-07-2017.
 * Spring Data Mongo DB repository for plagiarism check requests
 */
public interface PlagCheckRequestsRepository extends MongoRepository<PlagCheckRequests, String> {
    Optional<PlagCheckRequests> findByAuthorFileNameAndAuthorWalletAddressAndUserFileNameAndUserWalletAddress(String authorFileName, String authorWalletAddress,
                                                                                                              String userFileName, String userWalletAddress);

}
