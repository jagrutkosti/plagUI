package com.plagui.modules.plagcheckrequests;

import com.plagui.modules.GenericResponse;

import java.util.List;

/**
 * Created by Jagrut on 03-07-2017.
 * Data transfer object for
 */
public class PlagCheckRequestsDTO extends GenericResponse{
    private PlagCheckRequests singleRequestObject;
    private List<PlagCheckRequests> requestsFromUser;
    private List<PlagCheckRequests> requestsToUser;
    private String fullText;
    private String keywords;

    public PlagCheckRequests getSingleRequestObject() {
        return singleRequestObject;
    }

    public void setSingleRequestObject(PlagCheckRequests singleRequestObject) {
        this.singleRequestObject = singleRequestObject;
    }

    public List<PlagCheckRequests> getRequestsFromUser() {
        return requestsFromUser;
    }

    public void setRequestsFromUser(List<PlagCheckRequests> requestsFromUser) {
        this.requestsFromUser = requestsFromUser;
    }

    public List<PlagCheckRequests> getRequestsToUser() {
        return requestsToUser;
    }

    public void setRequestsToUser(List<PlagCheckRequests> requestsToUser) {
        this.requestsToUser = requestsToUser;
    }

    public String getFullText() {
        return fullText;
    }

    public void setFullText(String fullText) {
        this.fullText = fullText;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }
}
