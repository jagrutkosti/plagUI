package com.plagui.modules.plagcheckrequests;

import com.plagui.domain.User;
import com.plagui.repository.PlagCheckRequestsRepository;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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

    public PlagCheckRequestsService(PlagCheckRequestsRepository plagCheckRequestsRepository) {
        this.plagCheckRequestsRepository = plagCheckRequestsRepository;
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
            request.setStatus(0);
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
        dbItem.setStatus(2);
        dbItem = plagCheckRequestsRepository.save(dbItem);
        if(dbItem.getStatus() == 2)
            response.setSuccess("Request rejected successfully!");
        else
            response.setError("Problems while updating DB item");
        response.setSingleRequestObject(dbItem);
        return response;
    }
}
