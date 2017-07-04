package com.plagui.repository;

import com.plagui.modules.plagcheckrequests.PlagCheckRequests;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

/**
 * Created by Jagrut on 03-07-2017.
 * Spring Data Mongo DB repository for plagiarism check requests
 */
public interface PlagCheckRequestsRepository extends MongoRepository<PlagCheckRequests, String> {

    Optional<PlagCheckRequests> findByAuthorFileNameAndAuthorWalletAddressAndUserFileNameAndUserWalletAddress(String authorFileName, String authorWalletAddress,
                                                                                                              String userFileName, String userWalletAddress);

    List<PlagCheckRequests> findAllByAuthorWalletAddressOrderByStatus(String authorWalletAddress);

    List<PlagCheckRequests> findAllByUserWalletAddressOrderByStatus(String userWalletAddress);

    List<PlagCheckRequests> findAllByAuthorWalletAddressAndStatus(String authorWalletAddress, int status);
}
