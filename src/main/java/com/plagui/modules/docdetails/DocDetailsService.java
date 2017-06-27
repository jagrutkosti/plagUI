package com.plagui.modules.docdetails;

import com.plagui.config.Constants;
import com.plagui.modules.UtilService;
import multichain.command.MultichainException;
import multichain.command.StreamCommand;
import multichain.object.StreamItem;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jagrut on 26-06-2017.
 * Methods to fetch details of document's hash submission status for a user.
 */
@Service
public class DocDetailsService {
    private final Logger log = LoggerFactory.getLogger(DocDetailsService.class);
    private final UtilService utilService;
    private DocDetailsDTO response;

    public DocDetailsService(UtilService utilService) {
        this.utilService = utilService;
    }

    /**
     * Gets all documents and its details for a user (identified by wallet address).
     * Calls other methods and aggregates the result into data transfer object
     * @param walletAddress the wallet address of the user for whom to fetch all documents and its details
     * @return {DocDetailsDTO} populated with published and unpublished work documents
     */
    public DocDetailsDTO getDocDetails(String walletAddress) {
        log.info("Fetching all documents for address, {}", walletAddress);
        response = new DocDetailsDTO();
        //Get both stream items for each user
        List<StreamItem> userItemsInPublishedWorkStream = getUserItemsFromStream(Constants.PUBLISHED_WORK_STREAM_NAME, walletAddress);
        List<StreamItem> userItemsInUnpublishedWorkStream = getUserItemsFromStream(Constants.UNPUBLISHED_WORK_STREAM_NAME, walletAddress);

        //Populate the items with the details from plag-detection module
        response.setPublishedWorkDocs(processStreamItemAndCreateResponse(userItemsInPublishedWorkStream));
        response.setUnpublishedWorkDocs(processStreamItemAndCreateResponse(userItemsInUnpublishedWorkStream));
        if(response.getError() == null || response.getError().length() <= 0)
            response.setSuccess("success");
        return response;
    }

    /**
     * For each stream item, query the transaction status from Plag detection module and get plagchain and
     * originstamp seed which the user can download. Other details fetched: Bitcoin address to which the transaction
     * was made, confirmation time. File name and submission time are populated directly from stream item.
     * @param streamItems List of stream items for which to fetch submission details
     * @return List<DocDetails> containing fields with relevant information
     */
    public List<DocDetails> processStreamItemAndCreateResponse(List<StreamItem> streamItems) {
        List<DocDetails> docDetailsList = new ArrayList<>();
        for(StreamItem streamItem : streamItems) {
            DocDetails docItem = new DocDetails();
            //Populate from stream item itself
            docItem.setSubmissionTimeToPlagchain(streamItem.getTime().toString() + "000");
            docItem.setFileName(utilService.transformDataFromHexToObject(streamItem.getData()).getFileName());
            docItem.setFileHash(streamItem.getKey());
            try {
                String responseStringFromPDModule = getSeedSubmissionDetailsForHash(streamItem.getKey());

                if(responseStringFromPDModule != null && responseStringFromPDModule.length() > 0 &&
                    responseStringFromPDModule.contains("{")) {
                    //For each hash, get the information from plag-detection module
                    JSONObject responseFromPDModule = new JSONObject(responseStringFromPDModule);
                    //If the stream item was identified by Plag-detection module, populate it, else do nothing
                    if(responseFromPDModule.get("seedDetails") == null) {
                        docItem.setFetchedByPDModule(false);
                    } else {
                        JSONObject seedDetails = responseFromPDModule.getJSONObject("seedDetails");
                        docItem.setPlagchainSeed(seedDetails.getString("plagchainSeed"));
                        docItem.setTransactionAddress(seedDetails.getString("originstampBtcAddress"));
                        docItem.setOriginstampSeed(seedDetails.getString("originstampSeed"));
                        docItem.setConfirmation(seedDetails.getInt("originstampConfirmed"));
                        docItem.setConfirmationTime(seedDetails.getString("originstampBitcoinConfirmTime"));
                        docItem.setFetchedByPDModule(true);
                    }
                } else {
                    response.setError(responseStringFromPDModule);
                    break;
                }
                docDetailsList.add(docItem);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return docDetailsList;
    }

    /**
     * Makes an HTTP request to Plag-detection module to fetch all relevant information about the given hash.
     * @param hashString the sha-256 hash for which to fetch the details
     * @return {String} JSON string containing response from the plag-detection server.
     */
    public String getSeedSubmissionDetailsForHash(String hashString) {
        HttpClient httpClient = HttpClients.createDefault();
        HttpPost postRequest = new HttpPost(Constants.SEED_DETAILS_FOR_HASH_URL);
        List<NameValuePair> paramList = new ArrayList<>();
        paramList.add(new BasicNameValuePair("hashString", hashString));
        try {
            postRequest.setEntity(new UrlEncodedFormEntity(paramList, "UTF-8"));
            HttpResponse response = httpClient.execute(postRequest);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            StringBuilder result = new StringBuilder();
            String line;
            while((line = bufferedReader.readLine()) != null)
                result.append(line);
            return result.toString();
        } catch (HttpHostConnectException e) {
            return "Plag detection server not available";
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Gets stream item for a user's wallet address from given stream in plagchain.
     * @param streamName the name of the stream into which to look into
     * @param walletAddress wallet address for which to fetch all items
     * @return List<StreamItem> containing all items for wallet address in the given stream
     */
    public List<StreamItem> getUserItemsFromStream(String streamName, String walletAddress) {
        log.info("Getting all user items for address: {}, from stream: {}", walletAddress, streamName);
        try {
            return StreamCommand.listStreamPublisherItems(streamName, walletAddress, "true");
        } catch (MultichainException e) {
            e.printStackTrace();
            return null;
        }
    }
}
