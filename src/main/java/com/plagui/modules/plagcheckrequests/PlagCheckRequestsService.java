package com.plagui.modules.plagcheckrequests;

import com.plagui.repository.PlagCheckRequestsRepository;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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
     * @param userWalletAddress the wallet address of the user currently logged in, who made the request
     * @return PlagCheckRequests object populated with relevant details
     */
    public PlagCheckRequests createRequest(JSONObject docDetails, String userFileName, String userWalletAddress) {
        String authorFileName;
        String authorWalletAddress;
        PlagCheckRequests request = new PlagCheckRequests();
        try {
            authorFileName = docDetails.getString("fileName");
            authorWalletAddress = docDetails.getString("publisherWalletAddress");
            Optional<PlagCheckRequests> dbItem = getDBRequestObjectIfPresent(authorFileName, authorWalletAddress, userFileName, userWalletAddress);
            if(dbItem.isPresent())
                return dbItem.get();
            request.setAuthorFileName(authorFileName);
            request.setAuthorWalletAddress(authorWalletAddress);
            request.setUserFileName(userFileName);
            request.setUserWalletAddress(userWalletAddress);
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
        return plagCheckRequestsRepository.findByAuthorFileNameAndAuthorWalletAddressAndUserFileNameAndUserWalletAddress(authorFileName, authorWalletAddress, userFileName, userWalletAddress);
    }
}
