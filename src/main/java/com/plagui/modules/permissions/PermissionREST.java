package com.plagui.modules.permissions;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.plagui.domain.User;
import com.plagui.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

/**
 * Created by Jagrut on 10-07-2017.
 */
@RestController
@RequestMapping("/api/plagchain")
public class PermissionREST {
    private final Logger log = LoggerFactory.getLogger(PermissionREST.class);
    private final PermissionService permissionService;
    private final UserService userService;

    public PermissionREST(PermissionService permissionService, UserService userService) {
        this.permissionService = permissionService;
        this.userService = userService;
    }

    /**
     * REST request to get all permissions for the logged in user
     * @return StreamPermissionsDTO with populated fields
     */
    @GetMapping("/getPermissionsForUser")
    @ResponseBody
    public StreamPermissionsDTO getPermissionsForUser() {
        User loggedInUser = userService.getUserWithAuthorities();
        log.info("REST request to get all permissions for user : {}", loggedInUser.getLogin());
        return permissionService.getPermissionsForUser(loggedInUser);
    }

    /**
     * REST request to get permissions and requests for the logged in user
     * @return StreamPermissionsDTO with populated fields
     */
    @GetMapping("/getPermissionsAndRequestsForUser")
    @ResponseBody
    public StreamPermissionsDTO getPermissionsAndRequestsForUser() {
        User loggedInUser = userService.getUserWithAuthorities();
        log.info("REST request to get all permissions and requests for user :{}", loggedInUser.getLogin());
        StreamPermissionsDTO permissionsForUser = permissionService.getPermissionsForUser(loggedInUser);
        return permissionService.getUserRequests(permissionsForUser);
    }

    /**
     * REST request to create a permission request for a stream and specific user.
     * @param streamItem The stream item containing all data for which to generate the request
     * @param type permission  type i.e. admin or write
     * @return StreamPermissionsDTO with populated fields
     */
    @PostMapping("/requestPermission")
    @ResponseBody
    public StreamPermissionsDTO requestPermission(@RequestParam("stream") String streamItem,
                                                  @RequestParam("type") String type) {
        log.info("REST request to create a permission");
        Gson gson = new GsonBuilder().create();
        StreamPermissionRequests streamObject = gson.fromJson(streamItem, StreamPermissionRequests.class);
        return permissionService.requestPermission(streamObject, type);
    }

    @PostMapping("/rejectPermission")
    @ResponseBody
    public StreamPermissionsDTO rejectPermission(@RequestParam("streamRequest") String streamRequest) {
        log.info("REST request to reject a permission");
        User loggedInUser = userService.getUserWithAuthorities();
        Gson gson = new GsonBuilder().create();
        StreamPermissionRequests streamRequestObject = gson.fromJson(streamRequest, StreamPermissionRequests.class);
        return permissionService.rejectPermission(streamRequestObject, loggedInUser.getPlagchainWalletAddress());
    }

    @PostMapping("/grantPermission")
    @ResponseBody
    public StreamPermissionsDTO grantPermission(@RequestParam("streamRequest") String streamRequest) {
        log.info("REST request to grant a permission");
        User loggedInUser = userService.getUserWithAuthorities();
        Gson gson = new GsonBuilder().create();
        StreamPermissionRequests streamRequestObject = gson.fromJson(streamRequest, StreamPermissionRequests.class);
        return permissionService.grantPermission(streamRequestObject, loggedInUser.getPlagchainWalletAddress());
    }
}
