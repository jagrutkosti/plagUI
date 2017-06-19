package com.plagui.modules.UploadDocs;

import com.plagui.modules.GenericResponse;
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
@RequestMapping("/api/plagchain")
public class UploadDocsREST {

    private final Logger log = LoggerFactory.getLogger(UploadDocsREST.class);

    private final PlagchainService plagchainService;

    public UploadDocsREST(PlagchainService plagchainService) {
        this.plagchainService = plagchainService;
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
                                     @RequestParam(required = false, value = "contactInfo")String contactInfo,
                                     @RequestParam(required = false, value = "isunpublished")boolean isunpublished) {
        log.info("REST request to timestamp PDF file");
        GenericResponse response = new GenericResponse();
        if(pdfFile.getOriginalFilename().endsWith(".pdf")) {
            if(plagchainService.processAndTimestampDoc(pdfFile, contactInfo, isunpublished))
                response.setSuccess("Text extracted, hashed and transacted on 'plagchain' successfully");
            else
                response.setError("Problem in processing file or during transaction");
        } else
            response.setError("File format not supported");
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
                                      @RequestParam(required = false, value = "contactInfo")String contactInfo,
                                      @RequestParam(required = false, value = "isunpublished")boolean isunpublished) {
        log.info("REST request to timestamp some text");
        GenericResponse response = new GenericResponse();
        if(plagchainService.processAndTimestampText(textToHash, contactInfo, isunpublished))
            response.setSuccess("Text received successfully");
        else
            response.setError("Problem in processing file or during transaction");
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
                                      @RequestParam(required = false, value = "contactInfo")String contactInfo) {
        log.info("REST request to timestamp image");
        GenericResponse response = new GenericResponse();
        if(plagchainService.processAndTimestampImage(imageFile, contactInfo))
            response.setSuccess("Text received successfully");
        else
            response.setError("Problem in processing file or during transaction");
        return response;
    }
}
