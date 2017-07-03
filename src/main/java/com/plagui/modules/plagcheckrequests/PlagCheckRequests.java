package com.plagui.modules.plagcheckrequests;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * Created by Jagrut on 03-07-2017.
 * DB object to store all information about the requests
 */
@Document(collection = "plagcheck_requests")
public class PlagCheckRequests implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @NotNull
    @Indexed
    @Field("user_wallet_address")
    private String userWalletAddress;

    @NotNull
    @Indexed
    @Field("author_wallet_address")
    private String authorWalletAddress;

    @NotNull
    @Field("user_file_name")
    private String userFileName;

    @NotNull
    @Field("author_file_name")
    private String authorFileName;

    @Field("user_hashes")
    private List<String> userHashes;

    @Field("author_hashes")
    private List<String> authorHashes;

    @NotNull
    @Field("minhash_simscore")
    private double minHashSimScore;

    @Field("sim_score")
    private double simScore;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserWalletAddress() {
        return userWalletAddress;
    }

    public void setUserWalletAddress(String userWalletAddress) {
        this.userWalletAddress = userWalletAddress;
    }

    public String getAuthorWalletAddress() {
        return authorWalletAddress;
    }

    public void setAuthorWalletAddress(String authorWalletAddress) {
        this.authorWalletAddress = authorWalletAddress;
    }

    public String getUserFileName() {
        return userFileName;
    }

    public void setUserFileName(String userFileName) {
        this.userFileName = userFileName;
    }

    public String getAuthorFileName() {
        return authorFileName;
    }

    public void setAuthorFileName(String authorFileName) {
        this.authorFileName = authorFileName;
    }

    public List<String> getUserHashes() {
        return userHashes;
    }

    public void setUserHashes(List<String> userHashes) {
        this.userHashes = userHashes;
    }

    public List<String> getAuthorHashes() {
        return authorHashes;
    }

    public void setAuthorHashes(List<String> authorHashes) {
        this.authorHashes = authorHashes;
    }

    public double getMinHashSimScore() {
        return minHashSimScore;
    }

    public void setMinHashSimScore(double minHashSimScore) {
        this.minHashSimScore = minHashSimScore;
    }

    public double getSimScore() {
        return simScore;
    }

    public void setSimScore(double simScore) {
        this.simScore = simScore;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PlagCheckRequests plagCheckRequests = (PlagCheckRequests) o;

        return authorWalletAddress.equals(plagCheckRequests.getAuthorWalletAddress()) && userWalletAddress.equals(plagCheckRequests.userWalletAddress)
            && authorFileName.equals(plagCheckRequests.authorFileName) && userFileName.equals(plagCheckRequests.userFileName);
    }

    @Override
    public String toString() {
        Gson gson = new GsonBuilder().create();
        return gson.toJson(this, this.getClass());
    }
}
