package com.plagui.modules.uploaddocs;

import com.plagui.domain.User;
import com.plagui.modules.GenericResponse;
import com.plagui.modules.UtilService;
import com.plagui.modules.plagcheck.PlagCheckResultDTO;
import com.plagui.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
                                     @RequestParam(required = false, value = "contactInfo")String contactInfo,
                                     @RequestParam(required = false, value = "isunpublished")boolean isunpublished) {
        log.info("REST request to timestamp PDF file");
        GenericResponse response = new GenericResponse();
        User currentUser = userService.getUserWithAuthorities();
        boolean recaptchaResponse = utilService.checkGoogleRecaptcha(gRecaptchaResponse);
        if(recaptchaResponse) {
            if(pdfFile.getOriginalFilename().endsWith(".pdf")) {
                String result = plagchainUploadService.processAndTimestampDoc(currentUser.getPlagchainWalletAddress(), pdfFile, contactInfo, isunpublished);
                if(!result.contains(" "))
                    response.setSuccess("success");
                else
                    response.setError(result);
            } else
                response.setError("File format not supported.");
        } else {
            response = new PlagCheckResultDTO();
            response.setError("Incorrect Google Recaptcha! Please try again.");
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
                                      @RequestParam(required = false, value = "contactInfo")String contactInfo,
                                      @RequestParam(required = false, value = "isunpublished")boolean isunpublished) {
        log.info("REST request to timestamp some text");
        GenericResponse response = new GenericResponse();
        User currentUser = userService.getUserWithAuthorities();
        boolean recaptchaResponse = utilService.checkGoogleRecaptcha(gRecaptchaResponse);
        if(recaptchaResponse) {
            String result = plagchainUploadService.processAndTimestampText(fileName, currentUser.getPlagchainWalletAddress(), textToHash, contactInfo, isunpublished);
            if(!result.contains(" "))
                response.setSuccess("success");
            else
                response.setError(result);
        } else {
            response = new PlagCheckResultDTO();
            response.setError("Incorrect Google Recaptcha! Please try again.");
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
                                       @RequestParam(required = false, value = "contactInfo")String contactInfo) {
        log.info("REST request to timestamp image");
        GenericResponse response = new GenericResponse();
        User currentUser = userService.getUserWithAuthorities();
        boolean recaptchaResponse = utilService.checkGoogleRecaptcha(gRecaptchaResponse);
        if(recaptchaResponse) {
            String result = plagchainUploadService.processAndTimestampImage(currentUser.getPlagchainWalletAddress(), imageFile, contactInfo);
            if(!result.contains(" "))
                response.setSuccess("success");
            else
                response.setError(result);
        } else {
            response = new PlagCheckResultDTO();
            response.setError("Incorrect Google Recaptcha! Please try again.");
        }
        return response;
    }
}
