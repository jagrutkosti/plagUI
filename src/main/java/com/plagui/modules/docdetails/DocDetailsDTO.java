package com.plagui.modules.docdetails;

import com.plagui.modules.GenericResponse;

import java.util.List;

/**
 * Created by Jagrut on 26-06-2017.
 * Response object for displaying document status.
 */
public class DocDetailsDTO extends GenericResponse {
    private List<DocDetails> publishedWorkDocs;
    private List<DocDetails> unpublishedWorkDocs;

    public List<DocDetails> getPublishedWorkDocs() {
        return publishedWorkDocs;
    }

    public void setPublishedWorkDocs(List<DocDetails> publishedWorkDocs) {
        this.publishedWorkDocs = publishedWorkDocs;
    }

    public List<DocDetails> getUnpublishedWorkDocs() {
        return unpublishedWorkDocs;
    }

    public void setUnpublishedWorkDocs(List<DocDetails> unpublishedWorkDocs) {
        this.unpublishedWorkDocs = unpublishedWorkDocs;
    }
}
