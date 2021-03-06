package com.plagui.modules.uploaddocs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.plagui.domain.User;
import com.plagui.modules.GenericResponse;
import com.plagui.modules.UtilService;
import com.plagui.modules.plagcheck.PlagCheckResultDTO;
import com.plagui.service.UserService;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jagrut on 07-06-2017.
 * REST controller to handle requests related to uploading documents, either for plagiarism check or for anchoring
 * on Bitcoin Blockchain
 */

@RestController
@RequestMapping("/api/plagchain/upload")
public class UploadDocsREST {

    private final Logger log = LoggerFactory.getLogger(UploadDocsREST.class);

    private final PlagchainUploadService plagchainUploadService;
    private final UserService userService;
    private final UtilService utilService;

    public UploadDocsREST(PlagchainUploadService plagchainUploadService, UserService userService, UtilService utilService) {
        this.plagchainUploadService = plagchainUploadService;
        this.userService = userService;
        this.utilService = utilService;
    }

    /**
     * REST request to extract text and images from PDF files.
     * Send them for data cleaning -> Generate Min Hash from cleaned data -> Write on plagchain
     * @param pdfFile the pdf file whose hash needs to be written on plagchain
     * @param contactInfo contact info of the user, if provided
     * @return {GenericResponse} object containing appropriate message
     */
    @PostMapping("/uploadDoc")
    @ResponseBody
    public GenericResponse uploadDoc(@RequestParam("fileToHash")MultipartFile pdfFile,
                                     @RequestParam("gRecaptchaResponse") String gRecaptchaResponse,
                                     @RequestParam("decryptedPrivKey")String decryptedPrivKey,
                                     @RequestParam(required = false, value = "contactInfo")String contactInfo,
                                     @RequestParam(required = false, value = "streamNames")String streamNames) {
        log.info("REST request to timestamp PDF file");
        Gson gson = new GsonBuilder().create();
        Type listType = new TypeToken<List<PDServersDTO>>(){}.getType();
        List<PDServersDTO> streamNamesList = gson.fromJson(streamNames, listType);

        GenericResponse response = new GenericResponse();
        User currentUser = userService.getUserWithAuthorities();
        boolean recaptchaResponse = utilService.checkGoogleRecaptcha(gRecaptchaResponse);
        if(recaptchaResponse) {
            if(pdfFile.getOriginalFilename().endsWith(".pdf")) {
                response = plagchainUploadService.processAndTimestampDoc(currentUser.getPlagchainAddress(), pdfFile, contactInfo, streamNamesList, decryptedPrivKey);
            } else
                response.setError("File format not supported.");
        } else {
            response = new GenericResponse();
            response.setError("Incorrect Recaptcha! Please try again.");
        }
        return response;
    }

    /**
     * REST request to timestamp the text in plagchain
     * Generate Min Hash from text -> Write on plagchain
     * @param textToHash the text from UI to be timestamped
     * @param contactInfo contact info of the user, if provided
     * @return {GenericResponse} object containing appropriate message
     */
    @PostMapping("/uploadText")
    @ResponseBody
    public GenericResponse uploadText(@RequestParam("textToHash")String textToHash,
                                      @RequestParam("fileName") String fileName,
                                      @RequestParam("gRecaptchaResponse") String gRecaptchaResponse,
                                      @RequestParam("decryptedPrivKey")String decryptedPrivKey,
                                      @RequestParam(required = false, value = "contactInfo")String contactInfo,
                                      @RequestParam(required = false, value = "streamNames")String streamNames) {
        log.info("REST request to timestamp some text");
        Gson gson = new GsonBuilder().create();
        Type listType = new TypeToken<List<PDServersDTO>>(){}.getType();
        List<PDServersDTO> streamNamesList = gson.fromJson(streamNames, listType);

        GenericResponse response;
        User currentUser = userService.getUserWithAuthorities();
        boolean recaptchaResponse = utilService.checkGoogleRecaptcha(gRecaptchaResponse);
        if(recaptchaResponse) {
            response = plagchainUploadService.processAndTimestampText(fileName, currentUser.getPlagchainAddress(), textToHash, contactInfo, streamNamesList, decryptedPrivKey);
        } else {
            response = new GenericResponse();
            response.setError("Incorrect Recaptcha! Please try again.");
        }
        return response;
    }

    /**
     * REST request to timestamp image in plagchain
     * Generate SHA256 Hash from image -> Write on plagchain
     * @param imageFile the image from UI to be timestamped
     * @param contactInfo contact info of the user, if provided
     * @return {GenericResponse} object containing appropriate message
     */
    @PostMapping("/uploadImage")
    @ResponseBody
    public GenericResponse uploadImage(@RequestParam("imageToHash")MultipartFile imageFile,
                                       @RequestParam("gRecaptchaResponse") String gRecaptchaResponse,
                                       @RequestParam("decryptedPrivKey")String decryptedPrivKey,
                                       @RequestParam(required = false, value = "contactInfo")String contactInfo) {
        log.info("REST request to timestamp image");
        GenericResponse response;
        User currentUser = userService.getUserWithAuthorities();
        boolean recaptchaResponse = utilService.checkGoogleRecaptcha(gRecaptchaResponse);
        if(recaptchaResponse) {
            response = plagchainUploadService.processAndTimestampImage(currentUser.getPlagchainAddress(), imageFile, contactInfo, decryptedPrivKey);
        } else {
            response = new GenericResponse();
            response.setError("Incorrect Recaptcha! Please try again.");
        }
        return response;
    }
}
