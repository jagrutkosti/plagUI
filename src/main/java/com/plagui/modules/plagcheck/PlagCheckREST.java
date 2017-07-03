package com.plagui.modules.plagcheck;

import com.plagui.modules.UtilService;
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
    private UtilService utilService;

    public PlagCheckREST(PlagCheckService plagCheckService, UtilService utilService) {
        this.plagCheckService = plagCheckService;
        this.utilService = utilService;
    }

    /**
     * REST request to handle plagiarism check for a document. Sub-calls made to plag detection module to run the
     * algorithm and return the result.
     * @param plagCheckDoc the document to check for plagiarism
     * @param checkUnpublishedWork if true, check unpublished work stream
     * @return {PlagCheckResultDTO} containing the result in JSON string. Will contain all matched along with
     *                              Jaccard similarity
     */
    @PostMapping("/plagCheckDoc")
    @ResponseBody
    public PlagCheckResultDTO plagCheckForDoc(@RequestParam("plagCheckDoc")MultipartFile plagCheckDoc,
                                              @RequestParam("gRecaptchaResponse") String gRecaptchaResponse,
                                              @RequestParam(required = false, value = "checkUnpublishedWork") boolean checkUnpublishedWork) {
        PlagCheckResultDTO response;
        boolean recaptchaResponse = utilService.checkGoogleRecaptcha(gRecaptchaResponse);
        if(recaptchaResponse) {
            if(plagCheckDoc.getOriginalFilename().endsWith(".pdf")) {
                response = plagCheckService.plagCheckForDoc(plagCheckDoc, checkUnpublishedWork);
                //Different kinds of error handling
                if(response != null && response.getError() == null)
                    response.setSuccess("success");
                else if(response != null && response.getError() == null) {
                    response = new PlagCheckResultDTO();
                    response.setError("Server error. Problems while submitting to plag-detection module.");
                } else if(response == null){
                    response = new PlagCheckResultDTO();
                    response.setError("No result received from plag detection server.");
                }
            } else {
                response = new PlagCheckResultDTO();
                response.setError("File format not supported.");
            }
        } else {
            response = new PlagCheckResultDTO();
            response.setError("Incorrect Recaptcha! Please try again.");
        }

        return response;
    }
}
