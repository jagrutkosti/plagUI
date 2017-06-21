package com.plagui.modules.PlagCheck;

import com.plagui.modules.GenericResponse;

/**
 * Created by Jagrut on 21-06-2017.
 * Data transfer object containing result of matched documents with JSON string format
 */
public class PlagCheckResultDTO extends GenericResponse {
    private String resultJsonString;

    public String getResultJsonString() {
        return resultJsonString;
    }

    public void setResultJsonString(String resultJsonString) {
        this.resultJsonString = resultJsonString;
    }
}
