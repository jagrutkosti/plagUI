package com.plagui.modules;

import com.plagui.domain.User;
import com.plagui.modules.uploaddocs.PDServersDTO;
import com.plagui.repository.UserRepository;
import com.plagui.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/plagchain")
public class GenericREST {
    private final Logger log = LoggerFactory.getLogger(GenericREST.class);

    private final UtilService utilService;
    private final UserService userService;
    private final UserRepository userRepository;

    public GenericREST(UtilService utilService, UserService userService, UserRepository userRepository) {
        this.utilService = utilService;
        this.userService = userService;
        this.userRepository = userRepository;
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

    @PostMapping("/getDocCheckPrice")
    @ResponseBody
    public int getDocCheckPrice(@RequestParam("plagchainAddress")String plagchainAddress) {
        log.info("GenericREST#getDocCheckPrice() for " + plagchainAddress);
        Optional<User> user = userRepository.findOneByPlagchainAddress(plagchainAddress);
        return user.map(User::getDocCheckPrice).orElse(0);
    }
}
