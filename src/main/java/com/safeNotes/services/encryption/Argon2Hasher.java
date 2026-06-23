package com.safeNotes.services.encryption;

import com.safeNotes.exceptions.HashingException;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

public class Argon2Hasher implements PasswordHasher {
    public byte[] hashPassword(String password) throws HashingException {
        Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);

        try {
            String hash = argon2.hash(10, 65536, 2, password.toCharArray());
            return hash.getBytes();
        }
        catch (Exception e) {
            throw new HashingException("Failed to hash password", e);
        }
        finally {
            argon2.wipeArray(password.toCharArray());
        }
    }

    @Override
    public boolean verify(String password, byte[] hash) throws HashingException {
        Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);
        try {
            String encodedHash = new String(hash);
            return argon2.verify(encodedHash, password.toCharArray());
        }
        catch (Exception e) {
            throw new HashingException("Failed to verify password.", e);
        }
    }

    @Override
    public boolean needsUpgrade(byte[] hash) {
        return false;
    }

    @Override
    public byte[] upgradeHash(String password, byte[] oldHash) throws HashingException {
        return hashPassword(password);
    }
}
