package com.plagui.modules.plagcheck;

import com.plagui.modules.GenericResponse;

/**
 * Created by Jagrut on 21-06-2017.
 * Data transfer object containing result of matched documents with JSON string format
 */
public class PlagCheckResultDTO extends GenericResponse {
    private String resultJsonString;
    private String plagCheckDocFileName;

    public String getResultJsonString() {
        return resultJsonString;
    }

    public void setResultJsonString(String resultJsonString) {
        this.resultJsonString = resultJsonString;
    }

    public String getPlagCheckDocFileName() {
        return plagCheckDocFileName;
    }

    public void setPlagCheckDocFileName(String plagCheckDocFileName) {
        this.plagCheckDocFileName = plagCheckDocFileName;
    }
}
