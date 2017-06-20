package com.plagui.modules.PlagCheck;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Jagrut on 20-06-2017.
 * REST controller to handle all requests related to plagiarism check for a document
 */
@RestController
@RequestMapping("/api/plagchain/plagcheck")
public class PlagCheckREST {
    private final Logger log = LoggerFactory.getLogger(PlagCheckREST.class);


}
