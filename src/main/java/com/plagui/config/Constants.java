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
    public static final int SHINGLE_LENGTH = 5;
    public static final String EN_SENTENCE_BIN_LOC = "src/main/resources/en-sent.bin";
    public static final String RANDOM_NUMBERS_FILE = "src/main/resources/random-numbers.txt";
    public static final int NUMBER_OF_RANDOM_NUMBERS = 199;

    private Constants() {
    }
}
