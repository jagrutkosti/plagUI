package com.plagui.modules;

import com.plagui.modules.uploaddocs.PDServersDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plagchain")
public class GenericREST {
    private final Logger log = LoggerFactory.getLogger(GenericREST.class);

    private final UtilService utilService;

    public GenericREST(UtilService utilService) {
        this.utilService = utilService;
    }

    @GetMapping("/getPdServersList")
    @ResponseBody
    public List<PDServersDTO> getPdServersList() {
        log.info("GenericREST#getPdServersList()");
        return utilService.getAllPDServers();
    }
}
