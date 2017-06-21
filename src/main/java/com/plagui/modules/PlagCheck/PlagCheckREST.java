package com.plagui.modules.PlagCheck;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Created by Jagrut on 20-06-2017.
 * REST controller to handle all requests related to plagiarism check for a document
 */
@RestController
@RequestMapping("/api/plagchain")
public class PlagCheckREST {
    private final Logger log = LoggerFactory.getLogger(PlagCheckREST.class);
    private PlagCheckService plagCheckService;

    public PlagCheckREST(PlagCheckService plagCheckService) {
        this.plagCheckService = plagCheckService;
    }

    @PostMapping("/plagCheckDoc")
    @ResponseBody
    public PlagCheckResultDTO plagCheckForDoc(@RequestParam("plagCheckDoc")MultipartFile plagCheckDoc,
                                              @RequestParam(required = false, value = "checkUnpublishedWork") boolean checkUnpublishedWork) {
        PlagCheckResultDTO response;
        if(plagCheckDoc.getOriginalFilename().endsWith(".pdf")) {
            response = plagCheckService.plagCheckForDoc(plagCheckDoc, checkUnpublishedWork);
            if(response != null)
                response.setSuccess("Performed calculations successfully");
            else{
                response = new PlagCheckResultDTO();
                response.setError("Server error. Problems while submitting to plag-detection module.");
            }

        } else {
            response = new PlagCheckResultDTO();
            response.setError("File format not supported");
        }

        return response;
    }
}
