package com.safeNotes.utils.security;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import com.safeNotes.exceptions.HashingException;

public class SecureRandomGenerator {
    public byte[] generateSalt() throws HashingException {
        byte[] salt = new byte[16];
        try {
            SecureRandom.getInstanceStrong().nextBytes(salt);
            return salt;
        }
        catch (NoSuchAlgorithmException e) {
            throw new HashingException("Failed to generate salt", e);
        }
    }
}
