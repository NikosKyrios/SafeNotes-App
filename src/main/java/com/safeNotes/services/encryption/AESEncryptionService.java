package com.safeNotes.services.encryption;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.safeNotes.exceptions.EncryptionException;
import com.safeNotes.utils.security.SecureRandomGenerator;

public class AESEncryptionService implements EncryptionService {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int KEY_SIZE = 32;
    private static final int GCM_TAG_LENGTH = 128;
    private static final int GCM_IV_LENGTH = 12;

    @Override
    public byte[] encrypt(String text, byte[] key) throws EncryptionException {
        try {
            if (text == null || key == null) {
                throw new EncryptionException("Text or key is null");
            }

            byte[] iv = SecureRandomGenerator.generateRandomBytes(GCM_IV_LENGTH);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, spec);

            byte[] encrypted = cipher.doFinal(text.getBytes(StandardCharsets.UTF_8));

            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

            return combined;
        }

        catch (Exception e) {
            throw new EncryptionException("Failed to encrypt: " + e.getMessage(), e);
        }
    }

    @Override
    public String decrypt(byte[] encryptedData, byte[] key) throws EncryptionException {
        try {
            if (encryptedData == null || key == null) {
                throw new EncryptionException("Encrypted data or key is null");
            }

            byte[] iv = new byte[GCM_IV_LENGTH];
            System.arraycopy(encryptedData, 0, iv, 0, iv.length);

            byte[] encrypted = new byte[encryptedData.length - iv.length];
            System.arraycopy(encryptedData, iv.length, encrypted, 0, encrypted.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, spec);

            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted, StandardCharsets.UTF_8);

        }
        catch (Exception e) {
            throw new EncryptionException("Failed to decrypt: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] generateKey(String password, byte[] salt) throws EncryptionException {
        try {
            if (password == null || salt == null) {
                throw new EncryptionException("Password or salt is null");
            }

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] combined = new byte[password.length() + salt.length];
            System.arraycopy(password.getBytes(StandardCharsets.UTF_8), 0, combined, 0, password.length());
            System.arraycopy(salt, 0, combined, password.length(), salt.length);

            byte[] keyBytes = digest.digest(combined);

            if (keyBytes.length > KEY_SIZE) {
                byte[] truncated = new byte[KEY_SIZE];
                System.arraycopy(keyBytes, 0, truncated, 0, KEY_SIZE);
                return truncated;
            }
            return keyBytes;
        }
        catch (Exception e) {
            throw new EncryptionException("Failed to generate key: " + e.getMessage(), e);
        }

    }


    @Override
    public byte[] generateSalt() {
        return SecureRandomGenerator.generateRandomBytes(16);
    }
}
