package com.plagui.domain;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.plagui.config.Constants;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.validator.constraints.Email;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.time.Instant;

/**
 * A user.
 */
@Document(collection = "jhi_user")
public class User extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @NotNull
    @Pattern(regexp = Constants.LOGIN_REGEX)
    @Size(min = 1, max = 50)
    @Indexed
    private String login;

    @JsonIgnore
    @NotNull
    @Size(min = 60, max = 60)
    private String password;

    @Size(max = 50)
    @Field("first_name")
    private String firstName;

    @Size(max = 50)
    @Field("last_name")
    private String lastName;

    @Email
    @Size(min = 5, max = 100)
    @Indexed
    private String email;

    private boolean activated = false;

    @Size(min = 2, max = 5)
    @Field("lang_key")
    private String langKey;

    @Size(max = 256)
    @Field("image_url")
    private String imageUrl;

    @Size(max = 20)
    @Field("activation_key")
    @JsonIgnore
    private String activationKey;

    @Size(max = 20)
    @Field("reset_key")
    @JsonIgnore
    private String resetKey;


    @Field("reset_date")
    private Instant resetDate = null;

    @JsonIgnore
    private Set<Authority> authorities = new HashSet<>();
    /*
    0 = Encrypt private key using login password and save in UI db
    1 = Save private key as is in UI db
    2 = User manages their own private key
     */
    @Field("privkey_option")
    private int privKeyOption;

    @Field("plagchain_address")
    private String plagchainAddress;

    @Field("plagchain_pubkey")
    private String plagchainPubkey;

    @Field("plagchain_privkey")
    private String plagchainPrivkey;

    @Field("associated_miner_address")
    private String associatedMinerAddress;

    @Field("associated_miner_name")
    private String associatedMinerName;

    @Field("doc_check_price")
    private int docCheckPrice;

    @Field("currency_balance")
    private int currencyBalance;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    //Lowercase the login before saving it in database
    public void setLogin(String login) {
        this.login = login.toLowerCase(Locale.ENGLISH);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean getActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public String getActivationKey() {
        return activationKey;
    }

    public void setActivationKey(String activationKey) {
        this.activationKey = activationKey;
    }

    public String getResetKey() {
        return resetKey;
    }

    public void setResetKey(String resetKey) {
        this.resetKey = resetKey;
    }

    public Instant getResetDate() {
       return resetDate;
    }

    public void setResetDate(Instant resetDate) {
       this.resetDate = resetDate;
    }
    public String getLangKey() {
        return langKey;
    }

    public void setLangKey(String langKey) {
        this.langKey = langKey;
    }

    public Set<Authority> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(Set<Authority> authorities) {
        this.authorities = authorities;
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

    public String getAssociatedMinerAddress() {
        return associatedMinerAddress;
    }

    public void setAssociatedMinerAddress(String associatedMinerAddress) {
        this.associatedMinerAddress = associatedMinerAddress;
    }

    public String getAssociatedMinerName() {
        return associatedMinerName;
    }

    public void setAssociatedMinerName(String associatedMinerName) {
        this.associatedMinerName = associatedMinerName;
    }

    public int getDocCheckPrice() {
        return docCheckPrice;
    }

    public void setDocCheckPrice(int docCheckPrice) {
        this.docCheckPrice = docCheckPrice;
    }

    public int getCurrencyBalance() {
        return currencyBalance;
    }

    public void setCurrencyBalance(int currencyBalance) {
        this.currencyBalance = currencyBalance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        User user = (User) o;

        return login.equals(user.login);
    }

    @Override
    public int hashCode() {
        return login.hashCode();
    }

    @Override
    public String toString() {
        Gson gson = new GsonBuilder().create();
        return gson.toJson(this, this.getClass());
    }
}
