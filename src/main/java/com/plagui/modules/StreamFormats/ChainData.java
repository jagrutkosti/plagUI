package com.plagui.modules.StreamFormats;

import java.util.List;

/**
 * Created by Jagrut on 19-06-2017.
 * For parsing the data field to a stream
 */
public class ChainData {
    private List<Integer> textMinHash;
    private List<String> imageHash;
    private String contactInfo;

    public List<Integer> getTextMinHash() {
        return textMinHash;
    }

    public void setTextMinHash(List<Integer> textMinHash) {
        this.textMinHash = textMinHash;
    }

    public List<String> getImageHash() {
        return imageHash;
    }

    public void setImageHash(List<String> imageHash) {
        this.imageHash = imageHash;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }
}
