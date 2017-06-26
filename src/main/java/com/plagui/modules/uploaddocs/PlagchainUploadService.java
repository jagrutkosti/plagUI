package com.plagui.modules.uploaddocs;

import com.plagui.config.Constants;
import com.plagui.modules.UtilService;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

/**
 * Created by Jagrut on 07-06-2017.
 * Service class to handle all logic and processing work related to the Plagchain
 */
@Service
public class PlagchainUploadService {
    private final Logger log = LoggerFactory.getLogger(PlagchainUploadService.class);
    private UtilService utilService;


    public PlagchainUploadService(UtilService utilService) {
        this.utilService = utilService;
    }

    /**
     * Calls other methods to process the document and submit it to block chain.
     * @param walletAddress the wallet address of the logged in user
     * @param pdfFile the Multipart file received from user
     * @param contactInfo contact info of the user, if provided
     * @return true, if everything was successful, exception otherwise
     */
    public String processAndTimestampDoc(String walletAddress, MultipartFile pdfFile, String contactInfo, boolean unpublishedWork) {
        log.info("Processing the pdf file for time stamping");
        //Calculate sha256 hash for document to be used as key
        List<String> sha256DocHash = new ArrayList<>();
        try {
            sha256DocHash = utilService.generateSHA256HashFromObjects(Arrays.asList(pdfFile.getBytes()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Parse pdf file for text and generate min hash
        String textFromPdf = utilService.parsePdf(pdfFile);
        String cleanedText = utilService.cleanText(textFromPdf);
        List<String> allShingles = new ArrayList<>();
        allShingles.addAll(utilService.createShingles(Constants.SHINGLE_LENGTH, cleanedText));
        int[] minHashFromShingles = utilService.generateMinHashSignature(allShingles);

        //Parse pdf file for images and generate sha256 hash
        List<byte[]> imagesFromPdfAsByte = utilService.extractImageFromPdfFile(pdfFile);
        List<String> sha256HashOfImages = utilService.generateSHA256HashFromObjects(imagesFromPdfAsByte);

        //Transform to Hex string format and submit to plagchain
        String hexData = utilService.formatDataToHex(pdfFile.getOriginalFilename(), Arrays.asList(ArrayUtils.toObject(minHashFromShingles)), sha256HashOfImages, contactInfo);
        if(unpublishedWork)
            return utilService.submitToPlagchain(walletAddress, Constants.UNPUBLISHED_WORK_STREAM_NAME, sha256DocHash.get(0), hexData);
        else
            return utilService.submitToPlagchain(walletAddress, Constants.PUBLISHED_WORK_STREAM_NAME, sha256DocHash.get(0), hexData);
    }

    /**
     * Calls other methods to process the text and submit it to block chain.
     * @param walletAddress the wallet address of the logged in user
     * @param textToHash the text submitted by user
     * @param contactInfo contact info of the user, if provided
     * @return true, if everything was successful, exception otherwise
     */
    public String processAndTimestampText(String fileName, String walletAddress, String textToHash, String contactInfo, boolean unpublishedWork) {
        log.info("Processing text for time stamping");
        //Calculate sha256 hash for document to be used as key
        List<String> sha256DocHash = utilService.generateSHA256HashFromObjects(Arrays.asList(textToHash.getBytes()));

        //Parse the text and generate min hash
        String cleanedText = utilService.cleanText(textToHash);
        List<String> allShingles = new ArrayList<>();
        allShingles.addAll(utilService.createShingles(Constants.SHINGLE_LENGTH, cleanedText));
        int[] minHashFromShingles = utilService.generateMinHashSignature(allShingles);

        //Transform to Hex string format and submit to plagchain
        String hexData = utilService.formatDataToHex(fileName, Arrays.asList(ArrayUtils.toObject(minHashFromShingles)), null, contactInfo);
        if(unpublishedWork)
            return utilService.submitToPlagchain(walletAddress, Constants.UNPUBLISHED_WORK_STREAM_NAME, sha256DocHash.get(0), hexData);
        else
            return utilService.submitToPlagchain(walletAddress, Constants.PUBLISHED_WORK_STREAM_NAME, sha256DocHash.get(0), hexData);
    }

    /**
     * Calls other methods to process the image and submit it to block chain.
     * @param walletAddress the wallet address of the logged in user
     * @param image the Multipart image file received from user
     * @param contactInfo contact info of the user, if provided
     * @return true, if everything was successful, exception otherwise
     */
    public String processAndTimestampImage(String walletAddress, MultipartFile image, String contactInfo) {
        log.info("Processing image for timestamping");
        //Calculate sha256 hash for document to be used as key
        List<String> sha256DocHash = new ArrayList<>();
        try {
            sha256DocHash = utilService.generateSHA256HashFromObjects(Arrays.asList(image.getBytes()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Transform to Hex string format and submit to plagchain
        String hexData = utilService.formatDataToHex(image.getName(), null, sha256DocHash, contactInfo);
        return utilService.submitToPlagchain(walletAddress, Constants.UNPUBLISHED_WORK_STREAM_NAME, sha256DocHash.get(0), hexData);
    }
}
