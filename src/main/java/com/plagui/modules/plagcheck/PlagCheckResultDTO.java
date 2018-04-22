package com.plagui.modules.plagcheck;

import com.plagui.modules.GenericResponse;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by Jagrut on 21-06-2017.
 * Data transfer object containing result of matched documents with JSON string format
 */
public class PlagCheckResultDTO extends GenericResponse {
    private HashMap<String, String> resultJsonString;
    private String plagCheckDocFileName;
    private String resultAsJson;

    public HashMap<String, String> getResultJsonString() {
        return resultJsonString;
    }

    public void setResultJsonString(HashMap<String, String> resultJsonString) {
        this.resultJsonString = resultJsonString;
    }

    public String getPlagCheckDocFileName() {
        return plagCheckDocFileName;
    }

    public void setPlagCheckDocFileName(String plagCheckDocFileName) {
        this.plagCheckDocFileName = plagCheckDocFileName;
    }

    public String getResultAsJson() {
        return resultAsJson;
    }

    public void setResultAsJson(String resultAsJson) {
        this.resultAsJson = resultAsJson;
    }
}
