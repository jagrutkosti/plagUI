package com.plagui.modules.plagcheckrequests;

import com.plagui.modules.GenericResponse;

import java.util.List;

/**
 * Created by Jagrut on 03-07-2017.
 * Data transfer object for
 */
public class PlagCheckRequestsDTO extends GenericResponse{
    private PlagCheckRequests singleRequestObject;
    private List<PlagCheckRequests> multipleRequestObject;

    public PlagCheckRequests getSingleRequestObject() {
        return singleRequestObject;
    }

    public void setSingleRequestObject(PlagCheckRequests singleRequestObject) {
        this.singleRequestObject = singleRequestObject;
    }

    public List<PlagCheckRequests> getMultipleRequestObject() {
        return multipleRequestObject;
    }

    public void setMultipleRequestObject(List<PlagCheckRequests> multipleRequestObject) {
        this.multipleRequestObject = multipleRequestObject;
    }
}
