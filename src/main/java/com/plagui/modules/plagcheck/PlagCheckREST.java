package com.plagui.modules.plagcheck;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.plagui.domain.User;
import com.plagui.modules.UtilService;
import com.plagui.modules.uploaddocs.PDServersDTO;
import com.plagui.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;

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
    private UserService userService;

    public PlagCheckREST(PlagCheckService plagCheckService, UtilService utilService, UserService userService) {
        this.plagCheckService = plagCheckService;
        this.utilService = utilService;
        this.userService = userService;
    }

    /**
     * REST request to handle plagiarism check for a document. Sub-calls made to plag detection module to run the
     * algorithm and return the result.
     * @param plagCheckDoc the document to check for plagiarism
     * @return {PlagCheckResultDTO} containing the result in JSON string. Will contain all matched along with
     *                              Jaccard similarity
     */
    @PostMapping("/plagCheckDoc")
    @ResponseBody
    public PlagCheckResultDTO plagCheckForDoc(@RequestParam("plagCheckDoc")MultipartFile plagCheckDoc,
                                              @RequestParam("decryptedPrivKey")String decryptedPrivKey,
                                              @RequestParam("gRecaptchaResponse") String gRecaptchaResponse,
                                              @RequestParam("streamNames")String streamNames) {
        log.info("REST request to check for document similarity");
        User currentUser = userService.getUserWithAuthorities();

        Gson gson = new GsonBuilder().create();
        Type listType = new TypeToken<List<PDServersDTO>>(){}.getType();
        List<PDServersDTO> streamNamesList = gson.fromJson(streamNames, listType);
        PlagCheckResultDTO response;
        boolean recaptchaResponse = utilService.checkGoogleRecaptcha(gRecaptchaResponse);

        if(recaptchaResponse) {
            if(plagCheckDoc.getOriginalFilename().endsWith(".pdf") || plagCheckDoc.getOriginalFilename().endsWith(".txt")) {
                response = plagCheckService.plagCheckForDoc(plagCheckDoc, streamNamesList, decryptedPrivKey, currentUser);
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
