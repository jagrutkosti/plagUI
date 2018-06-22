package com.plagui.config;

/**
 * Application constants.
 */
public final class Constants {

    //Regex for acceptable logins
    public static final String LOGIN_REGEX = "^[_'.@A-Za-z0-9-]*$";

    public static final String SYSTEM_ACCOUNT = "system";
    public static final String ANONYMOUS_USER = "anonymoususer";

    //Constants used for plagiarism detection system
    public static final int SHINGLE_LENGTH = 9;
    public static final String RANDOM_NUMBERS_FILE = "/random-numbers.txt";
    public static final int NUMBER_OF_RANDOM_NUMBERS = 199;
    public static final String CHAIN_NAME="plagchain";
    public static final String BITCOIN_ANCHOR_TIME = "86400000";
    public static final String TIMESTAMP_COLLECTION_NAME = "timestamps";
    public static final String ORIGINSTAMP_API_URL = "http://api.originstamp.org/api/";
    public static final String ORIGINSTAMP_API_KEY = "4c9be75a-f24c-4407-86b3-62e5c9f88aa3";

    public static final String TIMESTAMP_STREAM = "timestamp_new1";
    public static final String MINERS_STREAM = "all_miners";
    public static final String MINERS_USERS_ASSOCIATION_STREAM = "miner_association_new";
    public static final String PD_SERVERS_STREAM = "pd_servers_new";

    public static final String GOOGLE_RECAPTCHA_SECRET = "6LfGcycUAAAAAPUaHNoU-jatYvdgj75mjGUeYTzk";
    public static final String GOOGLE_RECAPTCHA_URL = "https://www.google.com/recaptcha/api/siteverify";

    public static final int REQUESTS_STATUS_PENDING = 0;
    public static final int REQUESTS_STATUS_ACCEPT = 1;
    public static final int REQUESTS_STATUS_REJECT = 2;
    public static final int REQUESTS_STATUS_COMPLETE = 3;

    public static final int PERMISSION_REQUESTED = 1;
    public static final int PERMISSION_GRANTED = 2;
    public static final int PERMISSION_REJECTED = 3;

    public static final float PERMISSION_CONSENSUS = 0.5f;

    //Constants to identify how the private key for the user is managed
    public static final int PRIVKEY_ENCRYPTED = 0;
    public static final int PRIVKEY_PLAIN_STORE = 1;
    public static final int PRIVKEY_USER_MANAGED = 2;

    private Constants() {
    }
}
