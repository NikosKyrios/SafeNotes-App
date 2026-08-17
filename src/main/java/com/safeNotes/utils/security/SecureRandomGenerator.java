package com.safeNotes.utils.security;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import com.safeNotes.exceptions.HashingException;

public class SecureRandomGenerator {
    public static byte[] generateSalt(int i) throws HashingException {
        byte[] salt = new byte[16];
        try {
            SecureRandom.getInstanceStrong().nextBytes(salt);
            return salt;
        }
        catch (NoSuchAlgorithmException e) {
            throw new HashingException("Failed to generate salt", e);
        }
    }

    public static byte[] generateRandomBytes(int length) {
        byte[] bytes = new byte[length];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(bytes);
        return bytes;
    }
}
