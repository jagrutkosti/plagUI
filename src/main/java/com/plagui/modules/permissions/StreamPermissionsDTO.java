package com.plagui.modules.permissions;

import com.plagui.modules.GenericResponse;

import java.util.List;

/**
 * Created by Jagrut on 10-07-2017.
 * Data transfer object for current user's stream permissions
 */
public class StreamPermissionsDTO extends GenericResponse{
    private List<StreamPermissionRequests> allStreamPermissionRequests;
    private List<StreamPermissionRequests> requestsForAdmin;
    private StreamPermissionRequests singleObject;

    public List<StreamPermissionRequests> getAllStreamPermissionRequests() {
        return allStreamPermissionRequests;
    }

    public void setAllStreamPermissionRequests(List<StreamPermissionRequests> allStreamPermissionRequests) {
        this.allStreamPermissionRequests = allStreamPermissionRequests;
    }

    public List<StreamPermissionRequests> getRequestsForAdmin() {
        return requestsForAdmin;
    }

    public void setRequestsForAdmin(List<StreamPermissionRequests> requestsForAdmin) {
        this.requestsForAdmin = requestsForAdmin;
    }

    public StreamPermissionRequests getSingleObject() {
        return singleObject;
    }

    public void setSingleObject(StreamPermissionRequests singleObject) {
        this.singleObject = singleObject;
    }
}
