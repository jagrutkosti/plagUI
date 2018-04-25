package com.plagui.modules.plagcheck;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.plagui.config.Constants;
import com.plagui.domain.User;
import com.plagui.modules.GenericPostRequest;
import com.plagui.modules.GenericResponse;
import com.plagui.modules.UtilService;
import com.plagui.modules.privatekeymanagement.PrivateKeyManagementService;
import com.plagui.modules.uploaddocs.PDServersDTO;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Jagrut on 20-06-2017.
 * Service method to process logic for plagiarism check
 */
@Service
public class PlagCheckService {
    private final Logger log = LoggerFactory.getLogger(PlagCheckService.class);
    private UtilService utilService;
    private PrivateKeyManagementService privateKeyManagementService;

    public PlagCheckService(UtilService utilService, PrivateKeyManagementService privateKeyManagementService) {
        this.utilService = utilService;
        this.privateKeyManagementService = privateKeyManagementService;
    }

    /**
     * Breaks down the PDF file into plain text, cleans the text and generates minhash. Calls callPlagDetectionModule()
     * to get results from plagdetection module.
     * @param plagCheckDoc pdf file to check for plagiarism
     * @return {PlagCheckResultDTO} containing the result in JSON string. Will contain all matched along with
     *                              Jaccard similarity
     */
    public PlagCheckResultDTO plagCheckForDoc(MultipartFile plagCheckDoc, List<PDServersDTO> submitToServers, String userPrivateKey, User loggedInUser) {
        log.info("Forward the df file to PD file for plagiarism check");
        File file = new File(System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") + plagCheckDoc.getOriginalFilename());
        try {
            plagCheckDoc.transferTo(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        GenericPostRequest requestParams = new GenericPostRequest();
        requestParams.setPdfFile(file);

        PlagCheckResultDTO allResponses = new PlagCheckResultDTO();
        HashMap<String, String> simDocFromServer = new HashMap<>();
        allResponses.setPlagCheckDocFileName(plagCheckDoc.getOriginalFilename());

        for(PDServersDTO server : submitToServers) {
            GenericResponse response = utilService.postRequestPDServer(requestParams, server.getCheckSimUrl(), "");
            if(response.getError() != null && response.getError().length() > 0)
                allResponses.setError(allResponses.getError().concat( "\n" + server.getPdServerName() + " : " + response.getError()));
            else
                simDocFromServer.put(server.getPdServerName(), response.getResponseText());

            //Transfer the currency
            privateKeyManagementService.sendCurrencyToPDServer(loggedInUser.getPlagchainAddress(),
                server.getPlagchainAddressForTransactions(), userPrivateKey, server.getSimCheckPriceInRawUnits());
        }
        allResponses.setResultJsonString(simDocFromServer);
        FileUtils.deleteQuietly(file.getParentFile());
        return allResponses;
    }

   /* *//**
     * Makes HTTP call to Plag detection module to fetch similar documents existing in the plagchain
     * @param docHash the sha-256 document hash
     * @param minHashList the min hash generated from the test file
     * @param imageHashList the list of sha-256 hash of the imagesw
     * @param checkUnpublishedWork if true, check unpublished work stream
     * @return {PlagCheckResultDTO} containing the result in JSON string. Will contain all matched along with
     *                              Jaccard similarity
     *//*
    public PlagCheckResultDTO callPlagDetectionModule(String docHash, List<Integer> minHashList, List<String> imageHashList, boolean checkUnpublishedWork) {
        log.info("Making HTTP call to Plagdetection module");
        PlagCheckResultDTO responseFromPlagdetection = new PlagCheckResultDTO();
        Gson gson = new GsonBuilder().create();
        String minHashListAsString = gson.toJson(minHashList);
        HttpClient httpClient = HttpClients.createDefault();
        HttpPost postRequest = new HttpPost(Constants.PLAGDETECTION_URL);


        List<NameValuePair> paramList = new ArrayList<>();
        paramList.add(new BasicNameValuePair("docHash", docHash));
        paramList.add(new BasicNameValuePair("textHashList", minHashListAsString.substring(1, minHashListAsString.length() - 1)));
        paramList.add(new BasicNameValuePair("imageHashList", gson.toJson(imageHashList)));
        paramList.add(new BasicNameValuePair("checkUnpublishedWorkStream", gson.toJson(checkUnpublishedWork)));

        try {
            postRequest.setEntity(new UrlEncodedFormEntity(paramList, "UTF-8"));
            HttpResponse response = httpClient.execute(postRequest);

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            StringBuilder result = new StringBuilder();
            String line;
            while((line = bufferedReader.readLine()) != null)
                result.append(line);
            responseFromPlagdetection.setResultJsonString(result.toString());
        } catch (HttpHostConnectException e) {
            responseFromPlagdetection.setError("Plag detection server not available");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return responseFromPlagdetection;
    }*/
}
