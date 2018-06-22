package com.plagui.modules.uploaddocs;

import com.plagui.config.Constants;
import com.plagui.modules.GenericPostRequest;
import com.plagui.modules.GenericResponse;
import com.plagui.modules.UtilService;
import com.plagui.modules.docdetails.Timestamps;
import com.plagui.modules.privatekeymanagement.PrivateKeyManagementService;
import com.plagui.repository.TimestampsRepository;
import multichain.command.MultichainException;
import multichain.command.StreamCommand;
import multichain.object.StreamItem;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
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
    private TimestampsRepository timestampsRepository;


    public PlagchainUploadService(UtilService utilService, PrivateKeyManagementService privateKeyManagementService,
                                  TimestampsRepository timestampsRepository) {
        this.utilService = utilService;
        this.privateKeyManagementService = privateKeyManagementService;
        this.timestampsRepository = timestampsRepository;
    }

    /**
     * Calls other methods to process the document and submit it to block chain.
     * @param walletAddress the wallet address of the logged in user
     * @param pdfFile the Multipart file received from user
     * @param contactInfo contact info of the user, if provided
     * @return true, if everything was successful, exception otherwise
     */
    public GenericResponse processAndTimestampDoc(String walletAddress, MultipartFile pdfFile, String contactInfo, List<PDServersDTO> submitToServers, String decryptedPrivKey) {
        log.info("Processing the pdf file for time stamping");
        GenericResponse genericResponse = new GenericResponse();
        List<String> sha256DocHash = new ArrayList<>();
        File file = new File(System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") + pdfFile.getOriginalFilename());
        try {
            pdfFile.transferTo(file);
            //Calculate sha256 hash for document to be used as key
            sha256DocHash = utilService.generateSHA256HashFromObjects(Arrays.asList(Files.readAllBytes(file.toPath())));
        } catch (IOException e) {
            e.printStackTrace();
        }

        String successResponse = "";
        String errorResponse = "";
        GenericPostRequest requestParams = new GenericPostRequest();
        requestParams.setContactInfo(contactInfo);
        requestParams.setPdfFile(file);
        requestParams.setFileName(pdfFile.getOriginalFilename());

        for(PDServersDTO server : submitToServers) {
            GenericResponse responseFromThisServer = utilService.postRequestPDServer(requestParams, server.getSubmitDocUrl(), walletAddress);
            if (responseFromThisServer.getError() != null && responseFromThisServer.getError().length() > 0)
                errorResponse = errorResponse.concat("\n" + server.getPdServerName() + " : " + responseFromThisServer.getError());
            else
                successResponse = successResponse.concat("\n" + server.getPdServerName() + " : " +responseFromThisServer.getResponseText());
        }

        if(!utilService.isDocumentInStream(Constants.TIMESTAMP_STREAM, sha256DocHash.get(0))) {
            //Save the item in DB
            Timestamps timestamp = new Timestamps();
            timestamp.setDocHashKey(sha256DocHash.get(0));
            timestamp.setPublisherAddress(walletAddress);
            timestamp.setFileName(pdfFile.getOriginalFilename());
            timestampsRepository.save(timestamp);

            //Submit the document for timestamp
            privateKeyManagementService.publishStreamItemForPrivateKey(walletAddress,decryptedPrivKey, Constants.TIMESTAMP_STREAM,
                sha256DocHash.get(0), utilService.formatDataToHex(timestamp.getFileName(), null, null, contactInfo));
            successResponse = successResponse.concat("\n" + Constants.TIMESTAMP_STREAM + " : Timestamped successfully");
        } else {
            errorResponse = errorResponse.concat("\n" + Constants.TIMESTAMP_STREAM + " : Document already timestamped");
        }
        genericResponse.setSuccess(successResponse);
        genericResponse.setError(errorResponse);

        FileUtils.deleteQuietly(file.getParentFile());
        return genericResponse;
    }

    /**
     * Calls other methods to process the text and submit it to block chain.
     * @param walletAddress the wallet address of the logged in user
     * @param textToHash the text submitted by user
     * @param contactInfo contact info of the user, if provided
     * @return true, if everything was successful, exception otherwise
     */
    public GenericResponse processAndTimestampText(String fileName, String walletAddress, String textToHash, String contactInfo, List<PDServersDTO> submitToServers, String decryptedPrivKey) {
        log.info("Processing text for time stamping");
        GenericResponse genericResponse = new GenericResponse();

        String successResponse = "";
        String errorResponse = "";

        //Calculate sha256 hash for document to be used as key
        List<String> sha256DocHash = utilService.generateSHA256HashFromObjects(Arrays.asList(textToHash.getBytes()));

        for(PDServersDTO server : submitToServers) {
            GenericPostRequest requestParams = new GenericPostRequest();
            requestParams.setContactInfo(contactInfo);
            requestParams.setTextualContent(textToHash);
            requestParams.setFileName(fileName);
            GenericResponse responseFromThisServer = utilService.postRequestPDServer(requestParams, server.getSubmitDocUrl(), walletAddress);
            if (responseFromThisServer.getError() != null && responseFromThisServer.getError().length() > 0)
                errorResponse = errorResponse.concat("\n" + server.getPdServerName() + " : " + responseFromThisServer.getError());
            else
                successResponse = successResponse.concat("\n" + server.getPdServerName() + " : " +responseFromThisServer.getResponseText());
        }

        //Submit the document for timestamp
        if(!utilService.isDocumentInStream(Constants.TIMESTAMP_STREAM, sha256DocHash.get(0))) {
            //Save the item in DB
            Timestamps timestamp = new Timestamps();
            timestamp.setDocHashKey(sha256DocHash.get(0));
            timestamp.setPublisherAddress(walletAddress);
            timestamp.setFileName(fileName);
            timestampsRepository.save(timestamp);

            //Submit the document for timestamp
            privateKeyManagementService.publishStreamItemForPrivateKey(walletAddress,decryptedPrivKey, Constants.TIMESTAMP_STREAM,
                sha256DocHash.get(0), utilService.formatDataToHex(timestamp.getFileName(), null, null, contactInfo));
            successResponse = successResponse.concat("\n" + Constants.TIMESTAMP_STREAM + " : Timestamped successfully");
        } else {
            errorResponse = errorResponse.concat("\n" + Constants.TIMESTAMP_STREAM + " : Document already timestamped");
        }
        genericResponse.setSuccess(successResponse);;
        genericResponse.setError(errorResponse);
        return genericResponse;
    }

    /**
     * Calls other methods to process the image and submit it to block chain.
     * @param walletAddress the wallet address of the logged in user
     * @param image the Multipart image file received from user
     * @param contactInfo contact info of the user, if provided
     * @return true, if everything was successful, exception otherwise
     */
    public GenericResponse processAndTimestampImage(String walletAddress, MultipartFile image, String contactInfo, String decryptedPrivKey) {
        log.info("Processing image for timestamping");
        GenericResponse genericResponse = new GenericResponse();
        String successResponse = "";
        String errorResponse = "";
        //Calculate sha256 hash for document to be used as key
        List<String> sha256DocHash = new ArrayList<>();
        try {
            sha256DocHash = utilService.generateSHA256HashFromObjects(Arrays.asList(image.getBytes()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(!utilService.isDocumentInStream(Constants.TIMESTAMP_STREAM, sha256DocHash.get(0))) {
            //Save the item in DB
            Timestamps timestamp = new Timestamps();
            timestamp.setDocHashKey(sha256DocHash.get(0));
            timestamp.setPublisherAddress(walletAddress);
            timestamp.setFileName(image.getOriginalFilename());
            timestampsRepository.save(timestamp);

            //Transform to Hex string format and submit to plagchain
            String hexData = utilService.formatDataToHex(image.getOriginalFilename(), null, sha256DocHash, contactInfo);
            privateKeyManagementService.publishStreamItemForPrivateKey(walletAddress,decryptedPrivKey, Constants.TIMESTAMP_STREAM, sha256DocHash.get(0), hexData);
            successResponse = successResponse.concat("\n" + Constants.TIMESTAMP_STREAM + " : Timestamped successfully");
        } else {
            errorResponse = errorResponse.concat("\n" + Constants.TIMESTAMP_STREAM + " : Document already timestamped");
        }
        genericResponse.setSuccess(successResponse);;
        genericResponse.setError(errorResponse);
        return genericResponse;
    }
}
