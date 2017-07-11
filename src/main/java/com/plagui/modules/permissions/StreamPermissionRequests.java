package com.plagui.modules.permissions;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Created by Jagrut on 10-07-2017.
 * Java object to structure permissions related to a stream.
 */
@Document(collection = "stream_permission")
public class StreamPermissionRequests implements Serializable{
    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @NotNull
    @Field("requester_wallet_address")
    private String requesterWalletAddress;

    @Field("requester_login")
    private String requesterLogin;

    @NotNull
    @Field("stream_name")
    private String streamName;

    @Field("write")
    private boolean write = false;

    @Field("admin")
    private boolean admin = false;

    /* 0 - no request made, 1 - requested, 2 - granted, 3 - rejected */
    @Field("write_request_status")
    private int writeRequestStatus = 0;

    @Field("admin_request_status")
    private int adminRequestStatus = 0;

    @Field("total_admins")
    private int totalAdmins;

    @Field("permission_granted")
    private int permissionGranted;

    @Field("permission_denied")
    private int permissionDenied;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRequesterWalletAddress() {
        return requesterWalletAddress;
    }

    public void setRequesterWalletAddress(String requesterWalletAddress) {
        this.requesterWalletAddress = requesterWalletAddress;
    }

    public String getRequesterLogin() {
        return requesterLogin;
    }

    public void setRequesterLogin(String requesterLogin) {
        this.requesterLogin = requesterLogin;
    }

    public String getStreamName() {
        return streamName;
    }

    public void setStreamName(String streamName) {
        this.streamName = streamName;
    }

    public boolean isWrite() {
        return write;
    }

    public void setWrite(boolean write) {
        this.write = write;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public int getWriteRequestStatus() {
        return writeRequestStatus;
    }

    public void setWriteRequestStatus(int writeRequestStatus) {
        this.writeRequestStatus = writeRequestStatus;
    }

    public int getAdminRequestStatus() {
        return adminRequestStatus;
    }

    public void setAdminRequestStatus(int adminRequestStatus) {
        this.adminRequestStatus = adminRequestStatus;
    }

    public int getTotalAdmins() {
        return totalAdmins;
    }

    public void setTotalAdmins(int totalAdmins) {
        this.totalAdmins = totalAdmins;
    }

    public int getPermissionGranted() {
        return permissionGranted;
    }

    public void setPermissionGranted(int permissionGranted) {
        this.permissionGranted = permissionGranted;
    }

    public int getPermissionDenied() {
        return permissionDenied;
    }

    public void setPermissionDenied(int permissionDenied) {
        this.permissionDenied = permissionDenied;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        StreamPermissionRequests streamPermissionRequests = (StreamPermissionRequests) o;

        return id.equals(streamPermissionRequests.getId());
    }

    @Override
    public String toString() {
        Gson gson = new GsonBuilder().create();
        return gson.toJson(this, this.getClass());
    }
}
