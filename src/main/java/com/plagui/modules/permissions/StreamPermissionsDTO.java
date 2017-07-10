package com.plagui.modules.permissions;

import com.plagui.modules.GenericResponse;

import java.util.List;

/**
 * Created by Jagrut on 10-07-2017.
 * Data transfer object for current user's stream permissions
 */
public class StreamPermissionsDTO extends GenericResponse{
    private List<StreamPermission> allStreamPermissions;

    public List<StreamPermission> getAllStreamPermissions() {
        return allStreamPermissions;
    }

    public void setAllStreamPermissions(List<StreamPermission> allStreamPermissions) {
        this.allStreamPermissions = allStreamPermissions;
    }
}
