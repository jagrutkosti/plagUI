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
    public static final int WORD_SHINGLE_LENGTH = 3;
    public static final String RANDOM_NUMBERS_FILE = "/random-numbers.txt";
    public static final int NUMBER_OF_RANDOM_NUMBERS = 199;
    public static final String CHAIN_NAME="plagchain";
    public static final String PUBLISHED_WORK_STREAM_NAME = "publishedwork";
    public static final String UNPUBLISHED_WORK_STREAM_NAME = "unpublishedwork";
    public static final String MINERS_STREAM = "allminers";
    public static final String MINERS_USERS_ASSOCIATION_STREAM = "minersusersassociation";
    public static final String PLAGDETECTION_URL = "http://localhost:8090/plagcheck/runMinHashAlgo";
    public static final String SEED_DETAILS_FOR_HASH_URL = "http://localhost:8090/plagcheck/getHashSeed";
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

    private Constants() {
    }
}
