package com.plagui.modules.uploaddocs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.Serializable;

public class PDServersDTO implements Serializable {
    private String pdServerName;
    private String pingUrl;
    private String submitDocUrl;
    private String checkSimUrl;
    private boolean selected = false;

    public String getPdServerName() {
        return pdServerName;
    }

    public void setPdServerName(String pdServerName) {
        this.pdServerName = pdServerName;
    }

    public String getPingUrl() {
        return pingUrl;
    }

    public void setPingUrl(String pingUrl) {
        this.pingUrl = pingUrl;
    }

    public String getSubmitDocUrl() {
        return submitDocUrl;
    }

    public void setSubmitDocUrl(String submitDocUrl) {
        this.submitDocUrl = submitDocUrl;
    }

    public String getCheckSimUrl() {
        return checkSimUrl;
    }

    public void setCheckSimUrl(String checkSimUrl) {
        this.checkSimUrl = checkSimUrl;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public String toString() {
        Gson gson = new GsonBuilder().create();
        return gson.toJson(this, this.getClass());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PDServersDTO that = (PDServersDTO) o;

        return pdServerName.equals(that.pdServerName);
    }

    @Override
    public int hashCode() {
        return pdServerName.hashCode();
    }
}
