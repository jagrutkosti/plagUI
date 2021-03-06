package com.plagui.service;

import com.plagui.domain.Authority;
import com.plagui.domain.User;
import com.plagui.modules.miners.MinersService;
import com.plagui.modules.privatekeymanagement.PrivateKeyManagementService;
import com.plagui.repository.AuthorityRepository;
import com.plagui.repository.PersistentTokenRepository;
import com.plagui.config.Constants;
import com.plagui.repository.UserRepository;
import com.plagui.security.AuthoritiesConstants;
import com.plagui.security.SecurityUtils;
import com.plagui.service.util.RandomUtil;
import com.plagui.service.dto.UserDTO;

import multichain.command.AddressCommand;
import multichain.command.ChainCommand;
import multichain.command.GrantCommand;
import multichain.command.MultichainException;
import multichain.object.Address;
import multichain.object.KeyPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class for managing users.
 */
@Service
public class UserService {

    private final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final PersistentTokenRepository persistentTokenRepository;

    private final AuthorityRepository authorityRepository;

    private final MinersService minersService;

    private final PrivateKeyManagementService privateKeyManagementService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, PersistentTokenRepository persistentTokenRepository,
                       AuthorityRepository authorityRepository, MinersService minersService, PrivateKeyManagementService privateKeyManagementService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.persistentTokenRepository = persistentTokenRepository;
        this.authorityRepository = authorityRepository;
        this.minersService = minersService;
        this.privateKeyManagementService = privateKeyManagementService;
    }

    public Optional<User> activateRegistration(String key) {
        log.debug("Activating user for activation key {}", key);
        return userRepository.findOneByActivationKey(key)
            .map(user -> {
                // activate given user for the registration key.
                user.setActivated(true);
                user.setActivationKey(null);
                userRepository.save(user);
                log.debug("Activated user: {}", user);
                return user;
            });
    }

    public Optional<User> completePasswordReset(String newPassword, String key) {
       log.debug("Reset user password for reset key {}", key);

       return userRepository.findOneByResetKey(key)
           .filter(user -> user.getResetDate().isAfter(Instant.now().minusSeconds(86400)))
           .map(user -> {
                user.setPassword(passwordEncoder.encode(newPassword));
                user.setResetKey(null);
                user.setResetDate(null);
                userRepository.save(user);
                return user;
           });
    }

    public Optional<User> requestPasswordReset(String mail) {
        return userRepository.findOneByEmail(mail)
            .filter(User::getActivated)
            .map(user -> {
                user.setResetKey(RandomUtil.generateResetKey());
                user.setResetDate(Instant.now());
                userRepository.save(user);
                return user;
            });
    }

    public User createUser(String login, String password, String firstName, String lastName, String email,
        String imageUrl, String langKey, int privKeyOption, String plagchainAddress, String plagchainPubkey, String plagchainPrivkey,
        String associatedMinerAddress, String associatedMinerName, int docPrice) {

        User newUser = new User();
        Authority authority = authorityRepository.findOne(AuthoritiesConstants.USER);
        Set<Authority> authorities = new HashSet<>();
        String encryptedPassword = passwordEncoder.encode(password);
        newUser.setLogin(login);
        // new user gets initially a generated password
        newUser.setPassword(encryptedPassword);
        newUser.setFirstName(firstName);
        newUser.setLastName(lastName);
        newUser.setEmail(email);
        newUser.setImageUrl(imageUrl);
        newUser.setLangKey(langKey);
        // new user is not active
        newUser.setActivated(true);
        // new user gets registration key
        newUser.setActivationKey(RandomUtil.generateActivationKey());
        authorities.add(authority);
        newUser.setAuthorities(authorities);

        //Set user address and all other plagchain options
        newUser.setPrivKeyOption(privKeyOption);
        newUser.setPlagchainAddress(plagchainAddress);
        newUser.setPlagchainPubkey(plagchainPubkey);
        newUser.setPlagchainPrivkey(plagchainPrivkey);
        newUser.setDocCheckPrice(docPrice);

        //Add the address to the wallet of the UI server and grant basic permissions
        privateKeyManagementService.importAddress(plagchainAddress);
        privateKeyManagementService.grantReceiveSendConnectPermission(plagchainAddress);
        privateKeyManagementService.sendZeroTransaction(plagchainAddress);

        //Store the associated miner info in DB as well as Blockchain
        newUser.setAssociatedMinerAddress(associatedMinerAddress);
        newUser.setAssociatedMinerName(associatedMinerName);
        minersService.addMinerAndUserAssociationToPlagchain(associatedMinerAddress, plagchainAddress);

        userRepository.save(newUser);
        log.debug("Created Information for User: {}", newUser);
        return newUser;
    }

    public User createUser(UserDTO userDTO) {
        User user = new User();
        user.setLogin(userDTO.getLogin());
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setEmail(userDTO.getEmail());
        user.setImageUrl(userDTO.getImageUrl());
        if (userDTO.getLangKey() == null) {
            user.setLangKey("en"); // default language
        } else {
            user.setLangKey(userDTO.getLangKey());
        }
        if (userDTO.getAuthorities() != null) {
            Set<Authority> authorities = new HashSet<>();
            userDTO.getAuthorities().forEach(
                authority -> authorities.add(authorityRepository.findOne(authority))
            );
            user.setAuthorities(authorities);
        }
        String encryptedPassword = passwordEncoder.encode(RandomUtil.generatePassword());
        user.setPassword(encryptedPassword);
        user.setResetKey(RandomUtil.generateResetKey());
        user.setResetDate(Instant.now());
        user.setActivated(true);

        //Set user address and all other plagchain options
        user.setPrivKeyOption(userDTO.getPrivKeyOption());
        user.setPlagchainAddress(userDTO.getPlagchainAddress());
        user.setPlagchainPubkey(userDTO.getPlagchainPubkey());
        user.setPlagchainPrivkey(userDTO.getPlagchainPrivkey());
        user.setDocCheckPrice(userDTO.getDocPrice());

        //Add the address to the wallet of the UI server and grant basic permissions
        privateKeyManagementService.importAddress(userDTO.getPlagchainAddress());
        privateKeyManagementService.grantReceiveSendConnectPermission(userDTO.getPlagchainAddress());
        privateKeyManagementService.sendZeroTransaction(userDTO.getPlagchainAddress());

        //Store the associated miner info in DB as well as Blockchain
        user.setAssociatedMinerAddress(userDTO.getSelectedMiner().getMinerAddress());
        user.setAssociatedMinerName(userDTO.getSelectedMiner().getMinerName());
        minersService.addMinerAndUserAssociationToPlagchain(userDTO.getSelectedMiner().getMinerAddress(), userDTO.getPlagchainAddress());

        userRepository.save(user);
        log.debug("Created Information for User: {}", user);
        return user;
    }

    /**
     * Update basic information (first name, last name, email, language) for the current user.
     *
     * @param firstName first name of user
     * @param lastName last name of user
     * @param email email id of user
     * @param langKey language key
     * @param imageUrl image URL of user
     */
    public void updateUser(String firstName, String lastName, String email, String langKey, String imageUrl) {
        userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin()).ifPresent(user -> {
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setEmail(email);
            user.setLangKey(langKey);
            user.setImageUrl(imageUrl);
            userRepository.save(user);
            log.debug("Changed Information for User: {}", user);
        });
    }

    /**
     * Update all information for a specific user, and return the modified user.
     *
     * @param userDTO user to update
     * @return updated user
     */
    public Optional<UserDTO> updateUser(UserDTO userDTO) {
        return Optional.of(userRepository
            .findOne(userDTO.getId()))
            .map(user -> {
                user.setLogin(userDTO.getLogin());
                user.setFirstName(userDTO.getFirstName());
                user.setLastName(userDTO.getLastName());
                user.setEmail(userDTO.getEmail());
                user.setImageUrl(userDTO.getImageUrl());
                user.setActivated(userDTO.isActivated());
                user.setLangKey(userDTO.getLangKey());
                Set<Authority> managedAuthorities = user.getAuthorities();
                managedAuthorities.clear();
                userDTO.getAuthorities().stream()
                    .map(authorityRepository::findOne)
                    .forEach(managedAuthorities::add);
                userRepository.save(user);
                log.debug("Changed Information for User: {}", user);
                return user;
            })
            .map(UserDTO::new);
    }

    public void deleteUser(String login) {
        userRepository.findOneByLogin(login).ifPresent(user -> {
            userRepository.delete(user);
            log.debug("Deleted User: {}", user);
        });
    }

    public void changePassword(String password) {
        userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin()).ifPresent(user -> {
            String encryptedPassword = passwordEncoder.encode(password);
            user.setPassword(encryptedPassword);
            userRepository.save(user);
            log.debug("Changed password for User: {}", user);
        });
    }

    public Page<UserDTO> getAllManagedUsers(Pageable pageable) {
        return userRepository.findAllByLoginNot(pageable, Constants.ANONYMOUS_USER).map(UserDTO::new);
    }

    public Optional<User> getUserWithAuthoritiesByLogin(String login) {
        return userRepository.findOneByLogin(login);
    }

    public User getUserWithAuthorities(String id) {
        return userRepository.findOne(id);
    }

    public User getUserWithAuthorities() {
        return userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin()).orElse(null);
    }

    /**
     * Persistent Token are used for providing automatic authentication, they should be automatically deleted after
     * 30 days.
     * <p>
     * This is scheduled to get fired everyday, at midnight.
     * </p>
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void removeOldPersistentTokens() {
        LocalDate now = LocalDate.now();
        persistentTokenRepository.findByTokenDateBefore(now.minusMonths(1)).forEach(token -> {
            log.debug("Deleting token {}", token.getSeries());
            persistentTokenRepository.delete(token);
        });
    }

    /**
     * Not activated users should be automatically deleted after 3 days.
     * <p>
     * This is scheduled to get fired everyday, at 01:00 (am).
     * </p>
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void removeNotActivatedUsers() {
        List<User> users = userRepository.findAllByActivatedIsFalseAndCreatedDateBefore(Instant.now().minus(3, ChronoUnit.DAYS));
        for (User user : users) {
            log.debug("Deleting not activated user {}", user.getLogin());
            userRepository.delete(user);
        }
    }

    /**
     * @return a list of all the authorities
     */
    public List<String> getAuthorities() {
        return authorityRepository.findAll().stream().map(Authority::getName).collect(Collectors.toList());
    }

    /**
     * Generates a new wallet address and corresponding public key for plagchin that can be associated with a user.
     * @return Address Multichain object containing public key and wallet address information
     */
    public Address generateNewPlagchainAddress() {
        Address newPlagchainAddress = new Address();
        try {
            newPlagchainAddress = AddressCommand.getNewAddress();
            GrantCommand.grant(newPlagchainAddress.getAddress(), "unpublishedwork.write");
        } catch (MultichainException e) {
            e.printStackTrace();
        }
        return newPlagchainAddress;
    }
}
