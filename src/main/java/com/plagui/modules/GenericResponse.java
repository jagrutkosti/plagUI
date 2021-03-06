package com.plagui.modules;

/**
 * Created by Jagrut on 07-06-2017.
 * A generic class to build a response.
 */
public class GenericResponse {
    private String responseText;
    private String success;
    private String error;

    public String getResponseText() {
        return responseText;
    }

    public void setResponseText(String responseText) {
        this.responseText = responseText;
    }

    public String getSuccess() {
        return success;
    }

    public void setSuccess(String success) {
        this.success = success;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
