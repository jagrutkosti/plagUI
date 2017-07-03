package com.plagui.modules.plagcheckrequests;

import com.plagui.domain.User;
import com.plagui.service.UserService;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    public PlagCheckRequestsDTO createPlagCheckRequest(@RequestParam("simDocDetails")JSONObject simDocDetails,
                                                       @RequestParam("plagCheckDocFileName")String plagCheckDocFileName) {
        log.info("REST request to create a plagiarism check request to author for file: {}", plagCheckDocFileName);
        User currentUser = userService.getUserWithAuthorities();
        PlagCheckRequestsDTO response = new PlagCheckRequestsDTO();
        response.setSingleRequestObject(plagCheckRequestsService.createRequest(simDocDetails, plagCheckDocFileName, currentUser.getPlagchainWalletAddress()));
        response.setSuccess("success");
        return response;
    }

}
