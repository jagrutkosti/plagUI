package com.plagui.modules.plagcheckrequests;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.plagui.domain.User;
import com.plagui.service.UserService;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Created by Jagrut on 03-07-2017.
 * REST controller to handle all request related to plagiarism check requests to the author.
 */
@RestController
@RequestMapping("/api/plagchain")
public class PlagCheckRequestsREST {
    private final Logger log = LoggerFactory.getLogger(PlagCheckRequestsREST.class);
    private final PlagCheckRequestsService plagCheckRequestsService;
    private final UserService userService;

    public PlagCheckRequestsREST(PlagCheckRequestsService plagCheckRequestsService, UserService userService) {
        this.plagCheckRequestsService = plagCheckRequestsService;
        this.userService = userService;
    }

    /**
     * REST request to create a request from the user to the publisher of a document (referred to as: author) for
     * checking the plagiarism using more rigorous technique
     * @param simDocDetails the details of the file of the author which had some similarity
     * @param plagCheckDocFileName the name of the file the user tested for plagiarism using minhash
     * @return PlagCheckRequestsDTO if the same request already exists, return the same object or create new request and
     * send the new object
     */
    @PostMapping("/createPlagCheckRequest")
    @ResponseBody
    public PlagCheckRequestsDTO createPlagCheckRequest(@RequestParam("simDocDetails")JSONObject simDocDetails,
                                                       @RequestParam("plagCheckDocFileName")String plagCheckDocFileName) {
        log.info("REST request to create a plagiarism check request to author for file: {}", plagCheckDocFileName);
        User currentUser = userService.getUserWithAuthorities();
        PlagCheckRequestsDTO response = new PlagCheckRequestsDTO();
        response.setSingleRequestObject(plagCheckRequestsService.createRequest(simDocDetails, plagCheckDocFileName, currentUser));
        response.setSuccess("success");
        return response;
    }

    /**
     * REST request to get all plagiarism check requests for this user.
     * @return PlagCheckRequestsDTO populated with all requests
     */
    @GetMapping("/getLoggedInUserRequests")
    @ResponseBody
    public PlagCheckRequestsDTO getLoggedInUserRequests() {
        log.info("REST request to get plagiarism check requests for logged in user");
        User currentUser = userService.getUserWithAuthorities();
        PlagCheckRequestsDTO response = new PlagCheckRequestsDTO();
        response.setRequestsFromUser(plagCheckRequestsService.getRequestsFromThisUser(currentUser));
        response.setRequestsToUser(plagCheckRequestsService.getRequestsToThisUser(currentUser));
        response.setSuccess("success");
        return response;
    }

    /**
     * REST request to return number of pending requests that were made to this user(author role in this scenario)
     * @return int number of pending requests
     */
    @GetMapping("/getPendingNumberOfRequests")
    public int getPendingNumberOfRequests() {
        log.info("REST request to get pending number of requests made to the logged in user");
        User currentUser = userService.getUserWithAuthorities();
        return plagCheckRequestsService.getPendingNumberOfRequests(currentUser);
    }

    /**
     * REST request to set the status of the request to "Rejected"
     * @param plagRequest the request whose status needs to be updated
     * @return PlagCheckRequestsDTO populated with response from server
     */
    @PostMapping("/rejectRequest")
    @ResponseBody
    public PlagCheckRequestsDTO rejectRequest(@RequestParam("plagRequest") String plagRequest) {
        log.info("REST request to reject the plagiarism check request: {}", plagRequest);
        Gson gson = new GsonBuilder().create();
        PlagCheckRequests rejectObject = gson.fromJson(plagRequest, PlagCheckRequests.class);
        return plagCheckRequestsService.rejectRequest(rejectObject);
    }

    /**
     * REST request to update the status of a request to Accepted and update the hashes of the requested document
     * @param plagRequest the request whose status and hashes needs to be updated
     * @param plagCheckDoc the document from which to calculate the hashes
     * @return PlagCheckRequestsDTO populated with response from server
     */
    @PostMapping("/acceptRequestWithDoc")
    public PlagCheckRequestsDTO acceptRequestWithDoc(@RequestParam("plagRequest") String plagRequest,
                                                     @RequestParam("plagCheckDoc")MultipartFile plagCheckDoc) {
        log.info("REST request to update the request status to accepted and generate hashes from the document");
        PlagCheckRequestsDTO response = new PlagCheckRequestsDTO();
        if(!plagCheckDoc.getOriginalFilename().endsWith(".pdf")) {
            response.setError("File format not supported. Please upload a PDF file");
            return response;
        }
        Gson gson = new GsonBuilder().create();
        PlagCheckRequests acceptObject = gson.fromJson(plagRequest, PlagCheckRequests.class);
        return plagCheckRequestsService.acceptRequestWithDoc(acceptObject, plagCheckDoc);
    }

    /**
     * REST request to compare the two document hashes and generate similarity score
     * @param plagRequest the request whose status and hashes needs to be updated
     * @param plagCheckUserDoc the document from which to calculate the hashes
     * @return PlagCheckRequestsDTO populated with response from server
     */
    @PostMapping("/userDocRequest")
    public PlagCheckRequestsDTO userDocRequest(@RequestParam("plagRequest") String plagRequest,
                                               @RequestParam("plagCheckUserDoc")MultipartFile plagCheckUserDoc) {
        log.info("REST request to compare the two document hashes and generate similarity score");
        PlagCheckRequestsDTO response = new PlagCheckRequestsDTO();
        if(!plagCheckUserDoc.getOriginalFilename().endsWith(".pdf")) {
            response.setError("File format not supported. Please upload a PDF file");
            return response;
        }
        Gson gson = new GsonBuilder().create();
        PlagCheckRequests userObject = gson.fromJson(plagRequest, PlagCheckRequests.class);
        return plagCheckRequestsService.userDocRequest(userObject, plagCheckUserDoc);
    }

}
