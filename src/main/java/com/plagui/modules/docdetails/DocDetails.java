package com.plagui.modules.docdetails;

/**
 * Created by Jagrut on 26-06-2017.
 * Java object to store details of single document.
 */
public class DocDetails {
    private String fileName;
    private String fileHash;
    private String plagchainSeed;
    private String originstampSeed;
    /*
     * fetchedByPDModule - true - stored in DB of plag-detection module, false otherwise.
     * confirmation:
     * 0- not submitted to bitcoin
     * 1- submitted to bitcoin
     * 2- included in block
     * 3- stamp verified and has one block above it.
     */
    private int confirmation;
    private boolean fetchedByPDModule;
    private String bitcoinAddress;
    private String transactionHash;
    private String plagchainSeedHash;
    private String confirmationTime;
    private String submissionTimeToPlagchain;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileHash() {
        return fileHash;
    }

    public void setFileHash(String fileHash) {
        this.fileHash = fileHash;
    }

    public String getPlagchainSeed() {
        return plagchainSeed;
    }

    public void setPlagchainSeed(String plagchainSeed) {
        this.plagchainSeed = plagchainSeed;
    }

    public String getOriginstampSeed() {
        return originstampSeed;
    }

    public void setOriginstampSeed(String originstampSeed) {
        this.originstampSeed = originstampSeed;
    }

    public int getConfirmation() {
        return confirmation;
    }

    public void setConfirmation(int confirmation) {
        this.confirmation = confirmation;
    }

    public boolean isFetchedByPDModule() {
        return fetchedByPDModule;
    }

    public void setFetchedByPDModule(boolean fetchedByPDModule) {
        this.fetchedByPDModule = fetchedByPDModule;
    }

    public String getConfirmationTime() {
        return confirmationTime;
    }

    public void setConfirmationTime(String confirmationTime) {
        this.confirmationTime = confirmationTime;
    }

    public String getSubmissionTimeToPlagchain() {
        return submissionTimeToPlagchain;
    }

    public void setSubmissionTimeToPlagchain(String submissionTimeToPlagchain) {
        this.submissionTimeToPlagchain = submissionTimeToPlagchain;
    }

    public String getBitcoinAddress() {
        return bitcoinAddress;
    }

    public void setBitcoinAddress(String bitcoinAddress) {
        this.bitcoinAddress = bitcoinAddress;
    }

    public String getTransactionHash() {
        return transactionHash;
    }

    public void setTransactionHash(String transactionHash) {
        this.transactionHash = transactionHash;
    }

    public String getPlagchainSeedHash() {
        return plagchainSeedHash;
    }

    public void setPlagchainSeedHash(String plagchainSeedHash) {
        this.plagchainSeedHash = plagchainSeedHash;
    }
}
