package com.plagui.modules.docdetails;

import com.plagui.domain.User;
import com.plagui.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Jagrut on 26-06-2017.
 * REST controller to handle requests for showing the documents uploaded by the user and their status.
 */
@RestController
@RequestMapping("/api/plagchain")
public class DocDetailsREST {
    private final Logger log = LoggerFactory.getLogger(DocDetailsREST.class);
    private final DocDetailsService docDetailsService;
    private final UserService userService;

    public DocDetailsREST(DocDetailsService docDetailsService, UserService userService){
        this.docDetailsService = docDetailsService;
        this.userService = userService;
    }

    /**
     * REST method to call for getting all documents and their details for logged in user.
     * @return DocDetailsDTO containing list of documents in different streams
     */
    @GetMapping("/getDocs")
    @ResponseBody
    public DocDetailsDTO getDocsDetailsForUser() {
        User currentUser = userService.getUserWithAuthorities();
        log.info("REST request to get all document details for: {}", currentUser.getLogin());

        return docDetailsService.getDocDetails(currentUser.getPlagchainAddress());
    }

}
