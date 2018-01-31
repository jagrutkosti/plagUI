package com.plagui.modules.miners;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.Serializable;
import java.util.List;

public class MinersDTO implements Serializable {
    private String minerName;
    private String minerAddress;
    private List<String> emailFormats;

    public String getMinerName() {
        return minerName;
    }

    public void setMinerName(String minerName) {
        this.minerName = minerName;
    }

    public String getMinerAddress() {
        return minerAddress;
    }

    public void setMinerAddress(String minerAddress) {
        this.minerAddress = minerAddress;
    }

    public List<String> getEmailFormats() {
        return emailFormats;
    }

    public void setEmailFormats(List<String> emailFormats) {
        this.emailFormats = emailFormats;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MinersDTO minersInfo = (MinersDTO) o;

        return minerAddress.equals(minersInfo.getMinerAddress());
    }

    @Override
    public String toString() {
        Gson gson = new GsonBuilder().create();
        return gson.toJson(this, this.getClass());
    }
}
