package com.plagui.modules.docdetails;

import com.plagui.config.Constants;
import com.plagui.modules.UtilService;
import com.plagui.repository.TimestampsRepository;
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
    private final SeedSubmissionService seedSubmissionService;
    private final TimestampsRepository timestampsRepository;
    private DocDetailsDTO response;

    public DocDetailsService(UtilService utilService, SeedSubmissionService seedSubmissionService,
                             TimestampsRepository timestampsRepository) {
        this.utilService = utilService;
        this.seedSubmissionService = seedSubmissionService;
        this.timestampsRepository = timestampsRepository;
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
        List<StreamItem> userItemsTimestampStream = getUserItemsFromStream(Constants.TIMESTAMP_STREAM, walletAddress);

        //Populate the items with the details from plag-detection module
        response.setTimestampedDocs(processStreamItemAndCreateResponse(userItemsTimestampStream));
        if(response.getError() == null || response.getError().length() <= 0)
            response.setSuccess("success");
        return response;
    }

    /**
     * For each stream item, query the transaction status from originstamp and get plagchain and
     * originstamp seed which the user can download. Other details fetched: Bitcoin address to which the transaction
     * was made, confirmation time. File name and submission time are populated directly from stream item.
     * @param streamItems List of stream items for which to fetch submission details
     * @return List<DocDetails> containing fields with relevant information
     */
    public List<DocDetails> processStreamItemAndCreateResponse(List<StreamItem> streamItems) {
        List<DocDetails> docDetailsList = new ArrayList<>();
        int count = 0;
        for(StreamItem streamItem : streamItems) {
            Timestamps dbItem = timestampsRepository.findTimestampsByDocHashKey(streamItem.getKey());

            DocDetails docItem = new DocDetails();
            //Populate from stream item itself
            docItem.setSubmissionTimeToPlagchain(streamItem.getTime().toString() + "000");
            docItem.setFileName(utilService.transformDataFromHexToObject(streamItem.getData()).getFileName());
            docItem.setFileHash(streamItem.getKey());

            //For each hash, get the information from plag-detection module
            SeedSubmission seedDetails = getSeedSubmissionDetailsForHash(streamItem.getKey());
            //If the stream item was identified by Plag-detection module, populate it, else do nothing
            if(seedDetails != null) {
                docItem.setPlagchainSeed(seedDetails.getPlagchainSeed());
                docItem.setBitcoinAddress(seedDetails.getOriginstampBtcAddress());
                docItem.setTransactionHash(seedDetails.getOriginstampTransactionHash());
                docItem.setOriginstampSeed(seedDetails.getOriginstampSeed());
                docItem.setConfirmation(seedDetails.getOriginstampConfirmed());
                docItem.setConfirmationTime(seedDetails.getOriginstampBitcoinConfirmTime());
                docItem.setFetchedByBitcoinSchedule(dbItem.isSubmittedToOriginstamp());
                docItem.setPlagchainSeedHash(seedDetails.getPlagchainSeedHash());
            }
            count++;
            docDetailsList.add(docItem);
        }
        if(count == 0)
            response.setError("No submission details found in local DB");
        return docDetailsList;
    }

    /**
     * Find Seed Submission hash in DB whose plagchain_seed filed contains the given hash
     * @param hashString the hash for which to find the corresponding seed
     * @return {SeedSubmission} object
     */
    public SeedSubmission getSeedSubmissionDetailsForHash(String hashString) {

        log.info("DocDetailsService to get seed submission object which contains the given hash");
        return seedSubmissionService.findByOriginstampSeedRegex(hashString);
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
