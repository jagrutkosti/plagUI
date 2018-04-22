package com.plagui.modules.uploaddocs;

import com.plagui.config.Constants;
import com.plagui.modules.GenericPostRequest;
import com.plagui.modules.GenericResponse;
import com.plagui.modules.UtilService;
import com.plagui.modules.privatekeymanagement.PrivateKeyManagementService;
import multichain.command.MultichainException;
import multichain.command.StreamCommand;
import multichain.object.StreamItem;
import org.apache.commons.lang3.ArrayUtils;
import org.json.JSONException;
import org.json.JSONObject;
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
    private PrivateKeyManagementService privateKeyManagementService;


    public PlagchainUploadService(UtilService utilService, PrivateKeyManagementService privateKeyManagementService) {
        this.utilService = utilService;
        this.privateKeyManagementService = privateKeyManagementService;
    }

    /**
     * Calls other methods to process the document and submit it to block chain.
     * @param walletAddress the wallet address of the logged in user
     * @param pdfFile the Multipart file received from user
     * @param contactInfo contact info of the user, if provided
     * @return true, if everything was successful, exception otherwise
     */
    public String processAndTimestampDoc(String walletAddress, MultipartFile pdfFile, String contactInfo, List<PDServersDTO> submitToServers, String decryptedPrivKey) {
        log.info("Processing the pdf file for time stamping");
        //Calculate sha256 hash for document to be used as key
        List<String> sha256DocHash = new ArrayList<>();
        try {
            sha256DocHash = utilService.generateSHA256HashFromObjects(Arrays.asList(pdfFile.getBytes()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        String response = "";
        GenericPostRequest requestParams = new GenericPostRequest();
        requestParams.setContactInfo(contactInfo);
        requestParams.setMultipartFile(pdfFile);

        for(PDServersDTO server : submitToServers) {
            System.out.println("inside");
            GenericResponse responseFromThisServer = utilService.postRequestPDServer(requestParams, server.getSubmitDocUrl(), walletAddress);
            if (responseFromThisServer.getError() != null && responseFromThisServer.getError().length() > 0)
                response = response.concat("\n" + server.getPdServerName() + " : " + responseFromThisServer.getError());
            else
                response = response.concat("\n" + server.getPdServerName() + " : " +responseFromThisServer.getResponseText());
        }

        //Submit the document for timestamp
        privateKeyManagementService.publishStreamItemForPrivateKey(walletAddress,decryptedPrivKey, Constants.TIMESTAMP_STREAM, sha256DocHash.get(0), "7b7d");
        return response;
    }

    /**
     * Calls other methods to process the text and submit it to block chain.
     * @param walletAddress the wallet address of the logged in user
     * @param textToHash the text submitted by user
     * @param contactInfo contact info of the user, if provided
     * @return true, if everything was successful, exception otherwise
     */
    public String processAndTimestampText(String fileName, String walletAddress, String textToHash, String contactInfo, List<PDServersDTO> submitToServers, String decryptedPrivKey) {
        log.info("Processing text for time stamping");
        String response = "";

        //Calculate sha256 hash for document to be used as key
        List<String> sha256DocHash = utilService.generateSHA256HashFromObjects(Arrays.asList(textToHash.getBytes()));

        for(PDServersDTO server : submitToServers) {
            GenericPostRequest requestParams = new GenericPostRequest();
            requestParams.setContactInfo(contactInfo);
            requestParams.setTextualContent(textToHash);
            requestParams.setFileName(fileName);
            GenericResponse responseFromThisServer = utilService.postRequestPDServer(requestParams, server.getSubmitDocUrl(), walletAddress);
            if (responseFromThisServer.getError() != null && responseFromThisServer.getError().length() > 0)
                response = response.concat("\n" + server.getPdServerName() + " : " + responseFromThisServer.getError());
            else
                response = response.concat("\n" + server.getPdServerName() + " : " +responseFromThisServer.getResponseText());
        }

        //Submit the document for timestamp
        privateKeyManagementService.publishStreamItemForPrivateKey(walletAddress,decryptedPrivKey, Constants.TIMESTAMP_STREAM, sha256DocHash.get(0), "7b7d");
        return response;
    }

    /**
     * Calls other methods to process the image and submit it to block chain.
     * @param walletAddress the wallet address of the logged in user
     * @param image the Multipart image file received from user
     * @param contactInfo contact info of the user, if provided
     * @return true, if everything was successful, exception otherwise
     */
    public String processAndTimestampImage(String walletAddress, MultipartFile image, String contactInfo, String decryptedPrivKey) {
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
        privateKeyManagementService.publishStreamItemForPrivateKey(walletAddress,decryptedPrivKey, Constants.TIMESTAMP_STREAM, sha256DocHash.get(0), hexData);
        return " ";
    }
}
