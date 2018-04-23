package com.plagui.modules.docdetails;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

@Document(collection = "timestamps")
public class Timestamps implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @Field("submitted_originstamp")
    private boolean submittedToOriginstamp = false;

    @NotNull
    @Field("doc_hash")
    private String docHashKey;

    @Field("file_name")
    private String fileName;

    @Field("publisher_address")
    private String publisherAddress;

    @Field("timestamp")
    private String timestamp;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isSubmittedToOriginstamp() {
        return submittedToOriginstamp;
    }

    public void setSubmittedToOriginstamp(boolean submittedToOriginstamp) {
        this.submittedToOriginstamp = submittedToOriginstamp;
    }

    public String getDocHashKey() {
        return docHashKey;
    }

    public void setDocHashKey(String docHashKey) {
        this.docHashKey = docHashKey;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getPublisherAddress() {
        return publisherAddress;
    }

    public void setPublisherAddress(String publisherAddress) {
        this.publisherAddress = publisherAddress;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Timestamps timestamps = (Timestamps) o;
        return Objects.equals(this.id, timestamps.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.id);
    }

    @Override
    public String toString() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this, this.getClass());
    }
}
