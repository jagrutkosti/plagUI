package com.plagui.modules.permissions;

/**
 * Created by Jagrut on 10-07-2017.
 * Java object to structure permissions related to a stream.
 */
public class StreamPermission {
    private String requesterWalletAddress;
    private String streamName;
    private boolean write;
    private boolean admin;
    /* 0 - no write access, 1 - write access requested, 2 - write access granted*/
    private int requestStatus;

    public String getRequesterWalletAddress() {
        return requesterWalletAddress;
    }

    public void setRequesterWalletAddress(String requesterWalletAddress) {
        this.requesterWalletAddress = requesterWalletAddress;
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

    public int getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(int requestStatus) {
        this.requestStatus = requestStatus;
    }
}
