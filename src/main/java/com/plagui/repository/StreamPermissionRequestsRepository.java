package com.plagui.repository;

import com.plagui.modules.permissions.StreamPermissionRequests;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

/**
 * Created by Jagrut on 10-07-2017.
 */
public interface StreamPermissionRequestsRepository extends MongoRepository<StreamPermissionRequests, String> {

    Optional<StreamPermissionRequests> findOneByRequesterWalletAddressAndStreamNameAndWriteRequestStatus(String walletAddress, String streamName, int writeRequestStatus);
    Optional<StreamPermissionRequests> findOneByRequesterWalletAddressAndStreamNameAndAdminRequestStatus(String walletAddress, String streamName, int adminRequestStatus);

    List<StreamPermissionRequests> findAllByStreamNameAndWriteRequestStatus(String streamName, int writeStatus);
    List<StreamPermissionRequests> findAllByStreamNameAndAdminRequestStatus(String streamName, int adminStatus);
}
