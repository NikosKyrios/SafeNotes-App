package com.safeNotes.services.encryption;

import com.safeNotes.exceptions.EncryptionException;

public interface EncryptionService {
    public byte[] encrypt(String text, byte[] key) throws EncryptionException;  
    public String decrypt(byte[] encryptedData, byte[] key) throws EncryptionException;
    public byte[] generateKey(String password, byte[] salt) throws EncryptionException;
    public byte[] generateSalt();
}
