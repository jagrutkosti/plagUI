package com.plagui.modules.permissions;

import com.plagui.domain.User;
import com.plagui.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

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
        return permissionService.getPermissionsForUser(loggedInUser.getPlagchainWalletAddress());
    }
}
