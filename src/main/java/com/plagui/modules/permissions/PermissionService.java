package com.plagui.modules.permissions;

import com.plagui.config.Constants;
import com.plagui.domain.User;
import com.plagui.repository.StreamPermissionRequestsRepository;
import multichain.command.GrantCommand;
import multichain.command.MultichainException;
import multichain.command.StreamCommand;
import multichain.object.Permission;
import multichain.object.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Created by Jagrut on 10-07-2017.
 * Service class to handle all methods related to managing permissions for all available streams in plagchain.
 */
@Service
public class PermissionService {
    private final Logger log = LoggerFactory.getLogger(PermissionService.class);
    private final StreamPermissionRequestsRepository streamPermissionRequestsRepository;

    public PermissionService(StreamPermissionRequestsRepository streamPermissionRequestsRepository) {
        this.streamPermissionRequestsRepository = streamPermissionRequestsRepository;
    }

    /**
     * Returns all permissions for all available streams in the blockchain for the mentioned user.
     * @param loggedInUser the currently logged in user object
     * @return StreamPermissionDTO object with populated fields
     */
    public StreamPermissionsDTO getPermissionsForUser(User loggedInUser) {
        log.info("Service method to get permissions for: {}", loggedInUser.getLogin());
        StreamPermissionsDTO response = new StreamPermissionsDTO();
        List<StreamPermissionRequests> allPermissionForUser = new ArrayList<>();

        //Get all streams available in the blockchain
        List<Stream> allStreams = getAllAvailableStreams();
        if(!allStreams.isEmpty()) {
            for(Stream stream : allStreams) {
                StreamPermissionRequests item = new StreamPermissionRequests();
                item.setRequesterWalletAddress(loggedInUser.getPlagchainWalletAddress());
                item.setRequesterLogin(loggedInUser.getLogin());
                item.setStreamName(stream.getName());
                //For each stream, if it is not public, check if the user has admin or write permissions and update
                if(!stream.isOpen()) {
                    List<Permission> permissionForStream = getPermissions(stream.getName() + ".*", loggedInUser.getPlagchainWalletAddress());
                    if(!permissionForStream.isEmpty()) {
                        for(Permission permission : permissionForStream) {
                            if(permission.getType().equalsIgnoreCase("write"))
                                item.setWrite(true);
                            else if(permission.getType().equalsIgnoreCase("admin"))
                                item.setAdmin(true);
                        }
                    }
                } else
                    item.setWrite(true);
                allPermissionForUser.add(item);
            }
        }
        response.setAllStreamPermissionRequests(allPermissionForUser);
        response.setSuccess("success");
        return response;
    }

    /**
     * Get requests available to user. This method returns all streams and associated user permissions.
     * It also returns permission requests made to the admin, for granting or rejecting the request.
     * @param permissionsForUser the permissions of a user, from getPermissionsForUser()
     * @return StreamPermissionDTO with updated request status and admin requests, if available
     */
    public StreamPermissionsDTO getUserRequests(StreamPermissionsDTO permissionsForUser) {
        log.info("Service method to get user requests");
        List<StreamPermissionRequests> requestsForAdmin = new ArrayList<>();

        for(StreamPermissionRequests permission : permissionsForUser.getAllStreamPermissionRequests()) {
            //If the user has admin rights, fetch all request for that stream and add it as requestsForAdmin
            if(permission.isAdmin()) {
                requestsForAdmin.addAll(streamPermissionRequestsRepository
                    .findAllByStreamNameAndAdminRequestStatus(permission.getStreamName(), Constants.PERMISSION_REQUESTED));
                requestsForAdmin.addAll(streamPermissionRequestsRepository
                    .findAllByStreamNameAndWriteRequestStatus(permission.getStreamName(), Constants.PERMISSION_REQUESTED));
            } else {
                //If user is not an admin, check if there is any admin request made and update the status accordingly
                Optional<StreamPermissionRequests> adminDbItem = streamPermissionRequestsRepository
                    .findOneByRequesterWalletAddressAndStreamNameAndAdminRequestStatus(permission.getRequesterWalletAddress(), permission.getStreamName(), Constants.PERMISSION_REQUESTED);
                adminDbItem.ifPresent(streamPermissionRequests -> permission.setAdminRequestStatus(streamPermissionRequests.getAdminRequestStatus()));

                //If the user does not have write permission, check if they made any write requests.
                if (!permission.isWrite()) {
                    Optional<StreamPermissionRequests> writeDbItem = streamPermissionRequestsRepository
                        .findOneByRequesterWalletAddressAndStreamNameAndWriteRequestStatus(permission.getRequesterWalletAddress(), permission.getStreamName(), Constants.PERMISSION_REQUESTED);
                    writeDbItem.ifPresent(streamPermissionRequests -> permission.setWriteRequestStatus(streamPermissionRequests.getWriteRequestStatus()));
                }
            }
        }
        if(!requestsForAdmin.isEmpty())
            permissionsForUser.setRequestsForAdmin(requestsForAdmin);
        return permissionsForUser;
    }

    /**
     * Creates a new permission request entry in the database. The request also contains total number of admin at the
     * time of creation of the request.
     * @param streamObject The stream object that needs to be stored as a request
     * @param type permission type i.e. admin or write
     * @return StreamPermissionsDTO with saved db item and status
     */
    public StreamPermissionsDTO requestPermission(StreamPermissionRequests streamObject, String type) {
        log.info("Service method to create a request object");
        StreamPermissionsDTO response = new StreamPermissionsDTO();
        //Get total number of current admins. For UI purposes, we need to show how many have granted or rejected
        streamObject.setTotalAdmins(getTotalNumberOfAdminsInStream(streamObject.getStreamName()));

        if(type.equalsIgnoreCase("admin")) {
            streamObject.setAdminRequestStatus(Constants.PERMISSION_REQUESTED);
            streamObject.setWriteRequestStatus(0);
        } else if(type.equalsIgnoreCase("write")) {
            streamObject.setWriteRequestStatus(Constants.PERMISSION_REQUESTED);
            streamObject.setAdminRequestStatus(0);
        }
        response.setSingleObject(streamPermissionRequestsRepository.save(streamObject));
        response.setSuccess("success");
        return response;
    }

    /**
     * Rejects permission from the logged in admin. Updates total number of admins, as it can change over time.
     * If rejections are more than the consensus threshold, set the reject flag, else only increment the counter.
     * @param streamRequestObject the stream request to reject
     * @param loggedInUserWalletAddress the admin who will execute the query
     * @return StreamPermissionsDTO with saved DB item and status
     */
    public StreamPermissionsDTO rejectPermission(StreamPermissionRequests streamRequestObject, String loggedInUserWalletAddress) {
        log.info("Service method to reject stream permission request");
        streamRequestObject.setTotalAdmins(getTotalNumberOfAdminsInStream(streamRequestObject.getStreamName()));
        streamRequestObject.getPermissionRejectedBy().add(loggedInUserWalletAddress);

        if(streamRequestObject.getPermissionRejectedBy().size() > Math.ceil(streamRequestObject.getTotalAdmins() * Constants.PERMISSION_CONSENSUS))
            if(Constants.PERMISSION_REQUESTED == streamRequestObject.getWriteRequestStatus())
                streamRequestObject.setWriteRequestStatus(Constants.PERMISSION_REJECTED);
            else
                streamRequestObject.setAdminRequestStatus(Constants.PERMISSION_REJECTED);
        StreamPermissionsDTO response = new StreamPermissionsDTO();
        response.setSingleObject(streamPermissionRequestsRepository.save(streamRequestObject));
        response.setSuccess("success");
        return response;
    }

    /**
     * Grants permission from the logged in user to the user who made the request. Updates total number of admins as it can change over time.
     * If grants are more or equal to consensus threshold, set the grant flag, else increment the counter.
     * @param streamRequestObject the stream request to grant
     * @param loggedInUserWalletAddress the admin who will execute the query
     * @return StreamPermissionsDTO with saved DB item and status
     */
    public StreamPermissionsDTO grantPermission(StreamPermissionRequests streamRequestObject, String loggedInUserWalletAddress) {
        log.info("Service method to grant stream permission request");
        streamRequestObject.setTotalAdmins(getTotalNumberOfAdminsInStream(streamRequestObject.getStreamName()));
        String permissionType = Constants.PERMISSION_REQUESTED == streamRequestObject.getWriteRequestStatus() ? "write" : "admin";
        String plagchainResponse = grantPermissionInChain(loggedInUserWalletAddress, streamRequestObject.getRequesterWalletAddress(), streamRequestObject.getStreamName(), permissionType);

        StreamPermissionsDTO response = new StreamPermissionsDTO();
        if(plagchainResponse != null && !plagchainResponse.isEmpty()) {
            if(Constants.PERMISSION_REQUESTED == streamRequestObject.getWriteRequestStatus()) {
                streamRequestObject.setWriteRequestStatus(Constants.PERMISSION_GRANTED);
                streamRequestObject.setWrite(true);
            }
            streamRequestObject.getPermissionGrantedBy().add(loggedInUserWalletAddress);
            if(streamRequestObject.getPermissionGrantedBy().size() >= Math.floor(streamRequestObject.getTotalAdmins() * Constants.PERMISSION_CONSENSUS)) {
                grantPermissionInChain(loggedInUserWalletAddress, streamRequestObject.getRequesterWalletAddress(), streamRequestObject.getStreamName(), "write");
                streamRequestObject.setAdminRequestStatus(Constants.PERMISSION_GRANTED);
                streamRequestObject.setAdmin(true);
            }
            response.setSingleObject(streamPermissionRequestsRepository.save(streamRequestObject));
            response.setSuccess("success");
        } else
            response.setError(plagchainResponse);
        return response;
    }

    /**
     * Fetch all streams in the blockchain
     * @return List of stream items
     */
    public List<Stream> getAllAvailableStreams() {
        log.info("Get all streams");
        try {
            return StreamCommand.listStreams();
        } catch (MultichainException e) {
            e.printStackTrace();
            return Collections.EMPTY_LIST;
        }
    }

    /**
     * Fetch all permission for the user in the given stream
     * @param streamName for which to check and get all permissions
     * @param address the user wallet address for which to check
     * @return List of permissions in the stream for the user
     */
    public List<Permission> getPermissions(String streamName, String address) {
        log.info("Get all permissions of a user in stream: {}", streamName);
        try {
            return GrantCommand.listPermissionForStreamAndAddress(streamName, address);
        } catch (MultichainException e) {
            e.printStackTrace();
            return Collections.EMPTY_LIST;
        }
    }

    /**
     * Grants permission to an address using admin address for given stream and permission type
     * @param fromAddress admin wallet address to grant permission
     * @param toAddress the address which requested the permission
     * @param streamName stream for which permission request was made
     * @param permissionType the type of permission i.e. admin, write
     * @return String transaction ID if the process was successful
     */
    public String grantPermissionInChain(String fromAddress, String toAddress, String streamName, String permissionType) {
        String response = "";
        try {
            response = GrantCommand.grantFrom(fromAddress, toAddress, streamName + "." + permissionType);
        } catch (MultichainException e) {
            e.printStackTrace();
        }
        return response;
    }

    /**
     * To get the updated number of admins every time.
     * @param streamName the stream for which to get admins
     * @return number of admins for current stream
     */
    public int getTotalNumberOfAdminsInStream(String streamName) {
        log.info("Get current total number of admins");
        List<Permission> permissionForStream = getPermissions(streamName + ".*", null);
        int adminCount = 0;
        for(Permission permission : permissionForStream) {
            if(permission.getType().equalsIgnoreCase("admin"))
                adminCount++;
        }
        return adminCount;
    }
}
