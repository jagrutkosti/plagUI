package com.plagui.modules.docdetails;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.originstamp.client.dto.OriginStampHash;
import com.originstamp.client.dto.OriginStampTableEntity;
import com.plagui.config.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.StringJoiner;

/**
 * Created by Jagrut on 29-05-2017.
 * Handles anchoring the confirmed hashes into Bitcoin using Originstamp service.
 */

@Component
public class BitcoinAnchorService {
    private final Logger log = LoggerFactory.getLogger(BitcoinAnchorService.class);

    @Value("${spring.data.mongodb.database}")
    private String databaseName;

    private TimestampsService timestampsService;
    private SeedSubmissionService seedSubmissionService;

    private MongoTemplate mongoTemplate;
    private ObjectMapper objectMapper;

    public BitcoinAnchorService(TimestampsService timestampsService, SeedSubmissionService seedSubmissionService) {
        this.timestampsService = timestampsService;
        this.seedSubmissionService = seedSubmissionService;
    }
    /**
     * Scheduled task to run which triggers other methods in this class
     * @throws IOException
     */
    @Scheduled(initialDelay = 1000, fixedRateString = Constants.BITCOIN_ANCHOR_TIME)
    public void startAnchoring() throws IOException {
        mongoTemplate = new MongoTemplate(new SimpleMongoDbFactory(new MongoClient(), databaseName));
        JaxbAnnotationModule jaxbAnnotationModule = new JaxbAnnotationModule();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(jaxbAnnotationModule);

        log.info("Start Anchoring");

        log.info("Checking confirmation of existing transactions");
        checkBitcoinConfirmationAndUpdate();
        log.info("Checking confirmation of existing transactions completed");

        log.info("Adding new doc hash to Bitcoin");
        anchorNewTransanctionToBitcoin();
        log.info("Adding new doc hash to Bitcoin completed");

        log.info("Anchoring process Completed");
    }

    /**
     * Checks for all non-confirmed hashes in the database if they are already confirmed by querying the
     * originstamp server.
     */
    public void checkBitcoinConfirmationAndUpdate() throws IOException {
        //Get all hashes that were submitted by the api key used by plagdetection module(this module)
        HashSet<OriginStampTableEntity.Hashes> allHashesForComparison = new HashSet<>();
        int sizeOfReceivedHashes;
        int offset = 0;
        int records = 250;
        do {
            //Request-Body requires offset and records. Get 250 results in one request (does not work with more than around 300 records).
            String requestBody = "{\"offset\":" + offset + ",\"records\":" + records + ",\"api_key\":\"" + Constants.ORIGINSTAMP_API_KEY + "\"}";
            String responseMultiHash = dataTransferOriginstamp(RequestMethod.POST, "table", false, null, requestBody);
            //Parse the response into object and extract only the hashes array and add them to our list of hashes.
            OriginStampTableEntity responseObject = objectMapper.readValue(responseMultiHash, OriginStampTableEntity.class);

            sizeOfReceivedHashes = responseObject.getHashes().size();
            allHashesForComparison.addAll(responseObject.getHashes());
            offset += records;
            //Repeat the request if there are more entries than 250.
        } while (sizeOfReceivedHashes > 249);

        //Query Database for all entries which are not confirmed
        List<Integer> unconfirmList = new ArrayList<>();
        unconfirmList.add(0);
        unconfirmList.add(1);
        unconfirmList.add(2);
        BasicDBObject query = new BasicDBObject("originstamp_confirmed", new BasicDBObject("$in", unconfirmList));
        DBCursor cursor = seedSubmissionService.find(query);
        while(cursor.hasNext()){
            SeedSubmission singleDocument = mongoTemplate.getConverter().read(SeedSubmission.class, cursor.next());
            //Find the document in the received list of hashes by comparing the hashes
            for(OriginStampTableEntity.Hashes singleResponseItem : allHashesForComparison) {
                if(singleResponseItem.getHashString().equals(singleDocument.getPlagchainSeedHash())) {

                     /* Check if the hash exist.
                     * 0- not submitted
                     * 1- submitted to bitcoin
                     * 2- included in block
                     * 3- stamp verified and has one block above it. */

                    if(singleResponseItem.getSubmitStatus().getMultiSeed() != null && singleResponseItem.getSubmitStatus().getMultiSeed() > 1) {
                        singleDocument.setOriginstampConfirmed(singleResponseItem.getSubmitStatus().getMultiSeed());

                        //Get bitcoin address to which the transaction was made. This address can be used to search the bitcoin blockchain
                        String responseSingleHash = dataTransferOriginstamp(RequestMethod.GET, "", true, singleResponseItem.getHashString(), null);
                        OriginStampHash responseSingleHashObject = objectMapper.readValue(responseSingleHash, OriginStampHash.class);
                        singleDocument.setOriginstampBtcAddress(responseSingleHashObject.getMultiSeed().getBitcoinAddress());
                        singleDocument.setOriginstampTransactionHash(responseSingleHashObject.getMultiSeed().getTransactionHash());
                        singleDocument.setOriginstampBitcoinConfirmTime(responseSingleHashObject.getMultiSeed().getTimestamp().toString());

                        //Get seed for this hash
                        if(singleDocument.getOriginstampSeed() == null || singleDocument.getOriginstampSeed().length() <= 0) {
                            String originstampSeed = dataTransferOriginstamp(RequestMethod.GET, "download/seed/", true, singleResponseItem.getHashString(), null);
                            singleDocument.setOriginstampSeed(originstampSeed);
                        }
                        seedSubmissionService.save(singleDocument);
                    } else if (singleResponseItem.getSubmitStatus().getMultiSeed() != null && singleResponseItem.getSubmitStatus().getMultiSeed() == 1) {
                        singleDocument.setOriginstampConfirmed(1);
                        //Get seed, when it was already submitted to bitcoin
                        String originstampSeed = dataTransferOriginstamp(RequestMethod.GET, "download/seed/", true, singleResponseItem.getHashString(), null);
                        singleDocument.setOriginstampSeed(originstampSeed);
                        seedSubmissionService.save(singleDocument);
                    }
                    break;
                }
            }
        }
    }

    /**
     * Anchor transactions into Bitcoin. Create " " separated seed of SHA-256 hash of all documents, then hash the seed
     * and submit the SHA-256 hash of the seed to the Originstamp server
     */
    public void anchorNewTransanctionToBitcoin() {
        StringJoiner seed = new StringJoiner(" ");
        BasicDBObject query = new BasicDBObject("submitted_originstamp", false);
        DBCursor cursor;
        int counter = 0;
        //Get transactions from respective DB items which are not submitted yet.
        cursor = timestampsService.find(query);
        while(cursor.hasNext()){
            Timestamps singleDocument = mongoTemplate.getConverter().read(Timestamps.class, cursor.next());
            //Append the document hash
            seed.add(singleDocument.getDocHashKey());
            counter++;
            //Set submittedToOriginstamp to true and save in DB
            singleDocument.setSubmittedToOriginstamp(true);
            timestampsService.save(singleDocument);
        }

        if(counter > 0) {
            //Get hash of the seed file
            String seedHash = generateSHA256HashFromString(seed.toString());
            //Submit to Originstamp for stamping the hash
            String responseFromOriginstamp = dataTransferOriginstamp(RequestMethod.POST, "", true, seedHash, "{}");

            //Save the seed information to DB
            SeedSubmission saveSeedToDB = new SeedSubmission();
            saveSeedToDB.setPlagchainSeed(seed.toString());
            saveSeedToDB.setPlagchainSeedHash(seedHash);
            seedSubmissionService.save(saveSeedToDB);

            log.info("Saved and Submitted Hash: {}", seedHash);
            log.info("Message From Server: {}", responseFromOriginstamp);
        } else
            log.info("No new hashes found");
    }

    /**
     * Generate SHA-256 hash of the submitted string
     * @param seed the string whose SHA-256 hash needs to be calculated
     * @return {String} Hash string generated
     */
    public String generateSHA256HashFromString(String seed) {
        log.info("Generating SHA-256 hash for: {}", seed);
        StringBuilder seedHash = new StringBuilder();
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(seed.getBytes());
            byte[] messageDigestBytes = messageDigest.digest();
            for(byte singleByte : messageDigestBytes) {
                String hex = Integer.toHexString(0xFF & singleByte);
                if(hex.length() == 1)
                    seedHash.append("0");
                seedHash.append(hex);
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return seedHash.toString();
    }

    /**
     * Transfers data to and from origin stamp server for the given Hash value
     * @param method GET or POST
     * @param endpoint the API endpoint to connect to.
     *                 (No leading separator. Add trailing separator (only if endpoint is not null) i.e. /)
     * @param hasRequestParam does this request contain request param e.g. hash_string
     * @param hashStringRequestParam the hash for which data should be fetched
     * @param jsonRequestBody for POST request only, attach the request_body. Should be in JSON form.
     * @return {String} the data from server in String
     */
    public String dataTransferOriginstamp (RequestMethod method, String endpoint,
                                           boolean hasRequestParam, String hashStringRequestParam,
                                           String jsonRequestBody){
        log.info("Transferring data to Originstamp server");
        StringBuilder completeUrl = new StringBuilder(Constants.ORIGINSTAMP_API_URL);
        completeUrl.append(endpoint);
        if(hasRequestParam)
            completeUrl.append(hashStringRequestParam);

        StringBuilder buffer = new StringBuilder();
        try {
            URL urlObject = new URL(completeUrl.toString());
            HttpURLConnection connection = (HttpURLConnection) urlObject.openConnection();
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", Constants.ORIGINSTAMP_API_KEY);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 ( compatible ) ");
            connection.setRequestProperty("Accept", "*/*");
            if(method == RequestMethod.POST) {
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                DataOutputStream dos = new DataOutputStream(connection.getOutputStream());
                if(jsonRequestBody != null && jsonRequestBody.length() > 0) {
                    dos.writeBytes(jsonRequestBody);
                    dos.close();
                }
            } else
                connection.setRequestMethod("GET");

            String line;
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer.toString();
    }
}
