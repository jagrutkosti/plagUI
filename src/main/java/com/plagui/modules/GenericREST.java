package com.plagui.modules;

import com.plagui.domain.User;
import com.plagui.modules.uploaddocs.PDServersDTO;
import com.plagui.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plagchain")
public class GenericREST {
    private final Logger log = LoggerFactory.getLogger(GenericREST.class);

    private final UtilService utilService;
    private final UserService userService;

    public GenericREST(UtilService utilService, UserService userService) {
        this.utilService = utilService;
        this.userService = userService;
    }

    @GetMapping("/getPdServersList")
    @ResponseBody
    public List<PDServersDTO> getPdServersList() {
        log.info("GenericREST#getPdServersList()");
        return utilService.getAllPDServers();
    }

    @GetMapping("/getRealTimeBalanceForLoggedInUser")
    @ResponseBody
    public int getRealTimeBalanceForLoggedInUser() {
        log.info("GenericREST#getRealTimeBalanceForLoggedInUser()");
        User user = userService.getUserWithAuthorities();
        return utilService.getRealTimeBalance(user.getPlagchainAddress());

    }
}
