package com.plagui.modules.permissions;

import multichain.command.GrantCommand;
import multichain.command.MultichainException;
import multichain.command.StreamCommand;
import multichain.object.Permission;
import multichain.object.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jagrut on 10-07-2017.
 * Service class to handle all methods related to managing permissions for all available streams in plagchain.
 */
@Service
public class PermissionService {
    private final Logger log = LoggerFactory.getLogger(PermissionService.class);

    /**
     * Returns all permissions for all available streams in the blockchain for the mentioned user.
     * @param walletAddress the wallet address of the user
     * @return StreamPermissionDTO object with populated fields
     */
    public StreamPermissionsDTO getPermissionsForUser(String walletAddress) {
        log.info("Service method to get permissions for: {}", walletAddress);
        StreamPermissionsDTO response = new StreamPermissionsDTO();
        List<StreamPermission> allPermissionForUser = new ArrayList<>();

        List<Stream> allStreams = getAllAvailableStreams();
        if(allStreams != null && allStreams.size() > 0) {
            for(Stream stream : allStreams) {
                StreamPermission item = new StreamPermission();
                item.setRequesterWalletAddress(walletAddress);
                item.setStreamName(stream.getName());
                if(!stream.isOpen()) {
                    List<Permission> permissionForStream = getPermissions(stream.getName() + ".*", walletAddress);
                    if(permissionForStream != null && permissionForStream.size() > 0) {
                        for(Permission permission : permissionForStream) {
                            if(permission.getType().equalsIgnoreCase("write"))
                                item.setWrite(true);
                            if(permission.getType().equalsIgnoreCase("admin"))
                                item.setAdmin(true);
                        }
                        allPermissionForUser.add(item);
                    }
                } else {
                    item.setWrite(true);
                    allPermissionForUser.add(item);
                }
            }
        }
        response.setAllStreamPermissions(allPermissionForUser);
        response.setSuccess("success");
        return response;
    }

    /**
     * Fetch all streams in the blockchain
     * @return List of stream items
     */
    public List<Stream> getAllAvailableStreams() {
        try {
            return StreamCommand.listStreams();
        } catch (MultichainException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Fetch all permission for the user in the given stream
     * @param streamName for which to check and get all permissions
     * @param address the user wallet address for which to check
     * @return List of permissions in the stream for the user
     */
    public List<Permission> getPermissions(String streamName, String address) {
        try {
            return GrantCommand.listPermissionForStreamAndAddress(streamName, address);
        } catch (MultichainException e) {
            e.printStackTrace();
            return null;
        }
    }
}
