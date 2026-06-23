package com.safeNotes.services.encryption;

import com.safeNotes.exceptions.HashingException;

public interface PasswordHasher {
    public byte[] hashPassword(String password) throws HashingException;
    public boolean verify(String password, byte[] hash) throws HashingException;
    public boolean needsUpgrade(byte[] hash);
    public byte[] upgradeHash(String password, byte[] oldHash) throws HashingException;
}
