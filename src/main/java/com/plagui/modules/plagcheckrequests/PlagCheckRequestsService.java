package com.plagui.modules.plagcheckrequests;

import com.plagui.config.Constants;
import com.plagui.domain.User;
import com.plagui.modules.UtilService;
import com.plagui.repository.PlagCheckRequestsRepository;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by Jagrut on 03-07-2017.
 * A service method to handle business logic for requests from PlagCheckRequestsREST.
 */
@Service
public class PlagCheckRequestsService {
    private final Logger log = LoggerFactory.getLogger(PlagCheckRequestsService.class);
    private final PlagCheckRequestsRepository plagCheckRequestsRepository;
    private final UtilService utilService;

    public PlagCheckRequestsService(PlagCheckRequestsRepository plagCheckRequestsRepository, UtilService utilService) {
        this.plagCheckRequestsRepository = plagCheckRequestsRepository;
        this.utilService = utilService;
    }

    /**
     * Check if the same kind of request already exists. If true, return that request object else create a new request
     * and return that object
     * @param docDetails the details of the file of the author which had some similarity
     * @param userFileName the name of the file the user tested for plagiarism using minhash
     * @param currentUser the user currently logged in, who made the request
     * @return PlagCheckRequests object populated with relevant details
     */
    public PlagCheckRequests createRequest(JSONObject docDetails, String userFileName, User currentUser) {
        log.info("Service method to create or get the request");
        String authorFileName;
        String authorWalletAddress;
        PlagCheckRequests request = new PlagCheckRequests();
        try {
            authorFileName = docDetails.getString("fileName");
            authorWalletAddress = docDetails.getString("publisherWalletAddress");
            Optional<PlagCheckRequests> dbItem = getDBRequestObjectIfPresent(authorFileName, authorWalletAddress, userFileName, currentUser.getPlagchainWalletAddress());
            if(dbItem.isPresent())
                return dbItem.get();
            request.setAuthorFileName(authorFileName);
            request.setAuthorWalletAddress(authorWalletAddress);
            request.setUserFileName(userFileName);
            request.setUserWalletAddress(currentUser.getPlagchainWalletAddress());
            request.setUserLoginId(currentUser.getLogin());
            request.setStatus(Constants.REQUESTS_STATUS_PENDING);
            request.setMinHashSimScore(docDetails.getDouble("similarityScore"));
            return plagCheckRequestsRepository.save(request);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Return the DB object as Optional.
     * @param authorFileName the name of the document that had some similarity
     * @param authorWalletAddress the wallet address of the publisher of the similar document
     * @param userFileName the name of the file that the user check for plagiarism
     * @param userWalletAddress the wallet address of the issuer of this request
     * @return Optional<PlagCheckRequests> DB item, if present.
     */
    public Optional<PlagCheckRequests> getDBRequestObjectIfPresent(String authorFileName, String authorWalletAddress, String userFileName, String userWalletAddress) {
        log.info("Service method to get Request object from DB");
        return plagCheckRequestsRepository.findByAuthorFileNameAndAuthorWalletAddressAndUserFileNameAndUserWalletAddress(authorFileName, authorWalletAddress, userFileName, userWalletAddress);
    }

    /**
     * Returns all requests that currently logged in user made to other users for plagiarism check
     * @param currentUser the User object of the logged in user
     * @return List of request made by logged in user
     */
    public List<PlagCheckRequests> getRequestsFromThisUser(User currentUser) {
        log.info("Service method to return all requests from the given user");
        return plagCheckRequestsRepository.findAllByUserWalletAddressOrderByStatus(currentUser.getPlagchainWalletAddress());
    }

    /**
     * Returns all requests that were made to this user for plagiarism check
     * @param currentUser the User object of the logged in user
     * @return List of requests made to the logged in user
     */
    public List<PlagCheckRequests> getRequestsToThisUser(User currentUser) {
        log.info("Service method to return all requests to this user");
        return plagCheckRequestsRepository.findAllByAuthorWalletAddressOrderByStatus(currentUser.getPlagchainWalletAddress());
    }

    /**
     * Returns the number of pending requests that were made to this logged in user
     * @param currentUser the User object of the logged in user
     * @return int number of pending requests made to this user
     */
    public int getPendingNumberOfRequests(User currentUser) {
        log.info("Service method to return the number of pending requests to this user");
        List<PlagCheckRequests> pendingRequests = plagCheckRequestsRepository.findAllByAuthorWalletAddressAndStatus(currentUser.getPlagchainWalletAddress(), 0);
        if(pendingRequests != null)
            return pendingRequests.size();
        return 0;
    }

    /**
     * Search the DB for the given object and update
     * @param rejectObject the object to update with reject flag i.e. status = 2
     * @return PlagCheckRequestsDTO populate the object with appropriate message
     */
    public PlagCheckRequestsDTO rejectRequest(PlagCheckRequests rejectObject) {
        log.info("Service method to reject the given request");
        PlagCheckRequestsDTO response = new PlagCheckRequestsDTO();
        PlagCheckRequests dbItem = plagCheckRequestsRepository.findOne(rejectObject.getId());
        dbItem.setStatus(Constants.REQUESTS_STATUS_REJECT);
        dbItem = plagCheckRequestsRepository.save(dbItem);
        if(dbItem.getStatus() == Constants.REQUESTS_STATUS_REJECT)
            response.setSuccess("Request rejected successfully!");
        else
            response.setError("Problems while updating DB item");
        response.setSingleRequestObject(dbItem);
        return response;
    }

    /**
     * Get hashes from document and update the DB item.
     * @param acceptObject the request to update with status and hashes
     * @param plagCheckDoc the document for which the request was generated
     * @return PlagCheckRequestsDTO populated with relevant fields
     */
    public PlagCheckRequestsDTO acceptRequestWithDoc(PlagCheckRequests acceptObject, MultipartFile plagCheckDoc) {
        log.info("Service method to update the db item with accept and generate and store hashes from file");
        PlagCheckRequestsDTO response = new PlagCheckRequestsDTO();
        List<String> sha256Hashes = getHashesFromDocument(plagCheckDoc);
        try {
            PlagCheckRequests dbItem = plagCheckRequestsRepository.findOne(acceptObject.getId());
            dbItem.setStatus(Constants.REQUESTS_STATUS_ACCEPT);
            dbItem.setAuthorHashes(sha256Hashes);
            response.setSingleRequestObject(plagCheckRequestsRepository.save(dbItem));
            response.setSuccess("Hashes generated successfully!");
        } catch (NullPointerException e) {
            e.printStackTrace();
            response.setError("Request does not exist anymore!");
        }

        return response;
    }

    /**
     * Get hashes from document, calculate similarity and update the DB item.
     * @param userObject the request to update with status and hashes
     * @param plagCheckUserDoc the document for which the request was generated
     * @return PlagCheckRequestsDTO populated with relevant fields
     */
    public PlagCheckRequestsDTO userDocRequest(PlagCheckRequests userObject, MultipartFile plagCheckUserDoc) {
        log.info("Service method to update the db item, calculate similarity and update DB item");
        PlagCheckRequestsDTO response = new PlagCheckRequestsDTO();
        List<String> sha256Hashes = getHashesFromDocument(plagCheckUserDoc);
        try {
            PlagCheckRequests dbItem = plagCheckRequestsRepository.findOne(userObject.getId());
            dbItem.setStatus(Constants.REQUESTS_STATUS_COMPLETE);
            dbItem.setUserHashes(sha256Hashes);
            dbItem.setSimScore(calculateJaccardCoefficient(dbItem));
            response.setSingleRequestObject(plagCheckRequestsRepository.save(dbItem));
            response.setSuccess("Hashes generated and similarity calculated successfully!");
        } catch (NullPointerException e) {
            e.printStackTrace();
            response.setError("Request does not exist anymore!");
        }

        return response;
    }

    /**
     * Calculates the Jaccard Coefficient between two list of hashes of author document and user document
     * @param plagRequest object containing list of hashes for user and author document
     * @return double similarity score
     */
    public double calculateJaccardCoefficient(PlagCheckRequests plagRequest) {
        int sameHashes = 0;
        List<String> authorDocHashes = plagRequest.getAuthorHashes();
        List<String> userDochashes = plagRequest.getUserHashes();
        for(String hash : authorDocHashes) {
            if(userDochashes.contains(hash))
                sameHashes++;
        }
        return sameHashes / (double)(authorDocHashes.size() + userDochashes.size() - sameHashes);
    }

    /**
     * Extract text from pdf, clean the text, generate hashes.
     * @param plagCheckDoc the document from which to calculate SHA-256 hashes for shingles
     * @return List<String> containing hashes for all shingles
     */
    public List<String> getHashesFromDocument(MultipartFile plagCheckDoc) {
        //Get text from PDF and clean the text
        String textFromPdf = utilService.parsePdf(plagCheckDoc);
        textFromPdf = utilService.cleanText(textFromPdf);
        //Generate word shingles from cleaned text
        List<String> shingles = utilService.createWordShingles(Constants.WORD_SHINGLE_LENGTH, textFromPdf);
        List<byte[]> byteArrayFromShingles = new ArrayList<>();
        for(String shingle : shingles) {
            byteArrayFromShingles.add(shingle.getBytes());
        }
        //Generate SHA-256 hash for each shingle and store in DB
        return utilService.generateSHA256HashFromObjects(byteArrayFromShingles);
    }

}
