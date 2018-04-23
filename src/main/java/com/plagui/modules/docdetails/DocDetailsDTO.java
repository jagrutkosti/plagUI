package com.plagui.modules.docdetails;

import com.plagui.modules.GenericResponse;

import java.util.List;

/**
 * Created by Jagrut on 26-06-2017.
 * Response object for displaying document status.
 */
public class DocDetailsDTO extends GenericResponse {
    private List<DocDetails> timestampedDocs;

    public List<DocDetails> getTimestampedDocs() {
        return timestampedDocs;
    }

    public void setTimestampedDocs(List<DocDetails> timestampedDocs) {
        this.timestampedDocs = timestampedDocs;
    }
}
