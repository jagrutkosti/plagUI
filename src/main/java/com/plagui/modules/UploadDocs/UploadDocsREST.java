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
     * @return {GenericResponse} object containing appropriate message
     */
    @PostMapping("/uploadDoc")
    @ResponseBody
    public GenericResponse uploadDoc(@RequestParam("fileToHash")MultipartFile pdfFile) {
        log.info("REST request to timestamp PDF file");
        GenericResponse response = new GenericResponse();
        if(pdfFile.getOriginalFilename().endsWith(".pdf")) {
            String textFromPdf = plagchainService.parsePdf(pdfFile);
            System.out.println(textFromPdf);
            response.setSuccess("Text extracted, hashed and transacted on 'plagchain' successfully");
        } else
            response.setError("File format not supported");

        return response;
    }

    /**
     * REST request to timestamp the text in plagchain
     * Generate Min Hash from text -> Write on plagchain
     * @param textToHash the text from UI to be timestamped
     * @return {GenericResponse} object containing appropriate message
     */
    @PostMapping("/uploadText")
    @ResponseBody
    public GenericResponse uploadText(@RequestParam("textToHash")String textToHash) {
        log.info("REST request to timestamp some text");
        GenericResponse response = new GenericResponse();
        System.out.println(textToHash);
        response.setSuccess("Text received successfully");
        return response;
    }
}
