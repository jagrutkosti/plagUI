package com.plagui.service.dto;

import com.plagui.config.Constants;

import com.plagui.domain.Authority;
import com.plagui.domain.User;

import com.plagui.modules.miners.MinersDTO;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.*;
import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A DTO representing a user, with his authorities.
 */
public class UserDTO {

    private String id;

    @NotBlank
    @Pattern(regexp = Constants.LOGIN_REGEX)
    @Size(min = 1, max = 50)
    private String login;

    @Size(max = 50)
    private String firstName;

    @Size(max = 50)
    private String lastName;

    @Email
    @Size(min = 5, max = 100)
    private String email;

    @Size(max = 256)
    private String imageUrl;

    private boolean activated = false;

    @Size(min = 2, max = 5)
    private String langKey;

    private String createdBy;

    private Instant createdDate;

    private String lastModifiedBy;

    private Instant lastModifiedDate;

    private Set<String> authorities;

    private int privKeyOption;

    private String plagchainAddress;

    private String plagchainPubkey;

    private String plagchainPrivkey;

    private MinersDTO selectedMiner;

    public UserDTO() {
        // Empty constructor needed for Jackson.
    }

    public UserDTO(User user) {
        this(user.getId(), user.getLogin(), user.getFirstName(), user.getLastName(),
            user.getEmail(), user.getActivated(), user.getImageUrl(), user.getLangKey(),
            user.getCreatedBy(), user.getCreatedDate(), user.getLastModifiedBy(), user.getLastModifiedDate(),
            user.getPrivKeyOption(), user.getPlagchainAddress(), user.getPlagchainPubkey(), user.getPlagchainPrivkey(),
            user.getAuthorities().stream().map(Authority::getName)
                .collect(Collectors.toSet()), user.getAssociatedMinerAddress(), user.getAssociatedMinerName());
    }

    public UserDTO(String id, String login, String firstName, String lastName,
        String email, boolean activated, String imageUrl, String langKey,
        String createdBy, Instant createdDate, String lastModifiedBy, Instant lastModifiedDate, int privKeyOption,
        String plagchainAddress, String plagchainPubkey, String plagchainPrivkey,
        Set<String> authorities, String associatedMinerAddress, String associatedMinerName) {

        MinersDTO selectedMiner = new MinersDTO();
        selectedMiner.setMinerAddress(associatedMinerAddress);
        selectedMiner.setMinerName(associatedMinerName);

        this.id = id;
        this.login = login;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.activated = activated;
        this.imageUrl = imageUrl;
        this.langKey = langKey;
        this.createdBy = createdBy;
        this.createdDate = createdDate;
        this.lastModifiedBy = lastModifiedBy;
        this.lastModifiedDate = lastModifiedDate;
        this.privKeyOption = privKeyOption;
        this.plagchainAddress = plagchainAddress;
        this.plagchainPubkey = plagchainPubkey;
        this.plagchainPrivkey = plagchainPrivkey;
        this.authorities = authorities;
        this.selectedMiner = selectedMiner;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public boolean isActivated() {
        return activated;
    }

    public String getLangKey() {
        return langKey;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public Instant getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Instant lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public Set<String> getAuthorities() {
        return authorities;
    }

    public MinersDTO getSelectedMiner() {
        return selectedMiner;
    }

    public int getPrivKeyOption() {
        return privKeyOption;
    }

    public void setPrivKeyOption(int privKeyOption) {
        this.privKeyOption = privKeyOption;
    }

    public String getPlagchainAddress() {
        return plagchainAddress;
    }

    public void setPlagchainAddress(String plagchainAddress) {
        this.plagchainAddress = plagchainAddress;
    }

    public String getPlagchainPubkey() {
        return plagchainPubkey;
    }

    public void setPlagchainPubkey(String plagchainPubkey) {
        this.plagchainPubkey = plagchainPubkey;
    }

    public String getPlagchainPrivkey() {
        return plagchainPrivkey;
    }

    public void setPlagchainPrivkey(String plagchainPrivkey) {
        this.plagchainPrivkey = plagchainPrivkey;
    }

    @Override
    public String toString() {
        return "UserDTO{" +
            "id='" + id + '\'' +
            ", login='" + login + '\'' +
            ", firstName='" + firstName + '\'' +
            ", lastName='" + lastName + '\'' +
            ", email='" + email + '\'' +
            ", imageUrl='" + imageUrl + '\'' +
            ", activated=" + activated +
            ", langKey='" + langKey + '\'' +
            ", createdBy='" + createdBy + '\'' +
            ", createdDate=" + createdDate +
            ", lastModifiedBy='" + lastModifiedBy + '\'' +
            ", lastModifiedDate=" + lastModifiedDate +
            ", authorities=" + authorities +
            ", privKeyOption=" + privKeyOption +
            ", plagchainAddress='" + plagchainAddress + '\'' +
            ", plagchainPubkey='" + plagchainPubkey + '\'' +
            ", plagchainPrivkey='" + plagchainPrivkey + '\'' +
            ", selectedMiner=" + selectedMiner +
            '}';
    }
}
