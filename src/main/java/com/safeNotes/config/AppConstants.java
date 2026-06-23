package com.safeNotes.config;

public class AppConstants {
    // Encryption
    public static final String ENCRYPTION_ALGORITHM = "AES/GCM/NoPadding";
    public static final int KEY_SIZE = 256; //bits 
    public static final int GCM_TAG_LENGTH = 128;
    public static final int SALT_LENGTH = 16; //bytes
    public static final int IV_LENGTH = 12;

    // Password Hashing
    public static final String HASH_ALGORITHM = "argon2";
    public static final int HASH_SALT_LENGTH = 16; 
    public static final int HASH_OUTPUT_LENGTH = 32;
    public static final int ARGON2_ITERATIONS = 10; //bytes
    public static final int ARGON2_MEMORY = 65536; //64 MB
    public static final int ARGON2_PARALLELISM = 2;

    // Keystroke metric
    public static final int KEYSTROKE_SAMPLES_REQUIRED = 2;
    public static final int KEYSTROKE_ADAPTATION_CYCLES = 6; 
    public static final double KEYSTROKE_INITIAL_THRESHOLD = 1.6;
    public static final double KEYSTROKE_THRESHOLD_CHANGE = 0.2; //bytes

    // Security
    public static final int MIN_PASSWORD_LENGTH = 10;
    public static final int MAX_LOGIN_ATTEMPTS = 5; 
    public static final long SESSION_TIMEOUT_MINUTES = 30;

    // File pathing
    public static final String DATA_DIR = System.getProperty("user.home") + "/.safenotes"; // for different os
    public static final String USERS_FILE = DATA_DIR + "/users.dat";
    public static final String NOTES_DIR = DATA_DIR + "/notes";
    public static final String KEYSTORE_FILE = DATA_DIR + "/keystore.jks";

    // Note security
    public static final int NOTE_PIN_LENGTH = 6;
    public static final int MAX_NOTE_SIZE_MB = 10; 

    // UI
    public static final String APP_TITLE = "SafeNotes";
    public static final String APP_VERSION = "1.0.0";
}
