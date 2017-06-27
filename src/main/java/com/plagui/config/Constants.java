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
    public static final String EN_SENTENCE_BIN_LOC = "src/main/resources/en-sent.bin";
    public static final String RANDOM_NUMBERS_FILE = "src/main/resources/random-numbers.txt";
    public static final int NUMBER_OF_RANDOM_NUMBERS = 199;
    public static final String CHAIN_NAME="plagchain";
    public static final String PUBLISHED_WORK_STREAM_NAME = "publishedwork";
    public static final String UNPUBLISHED_WORK_STREAM_NAME = "unpublishedwork";
    public static final String PLAGDETECTION_URL = "http://localhost:8090/plagcheck/runMinHashAlgo";
    public static final String SEED_DETAILS_FOR_HASH_URL = "http://localhost:8090/plagcheck/getHashSeed";

    private Constants() {
    }
}
