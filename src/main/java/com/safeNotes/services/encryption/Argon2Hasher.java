package com.safeNotes.services.encryption;

import java.util.Base64;

import com.safeNotes.exceptions.HashingException;
import com.safeNotes.utils.security.SecureRandomGenerator;

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

    @Override
    public String hashPin(String pin) throws HashingException {

        try {
            //byte[] salt = SecureRandomGenerator.generateSalt(8);

            Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);
            String hash = argon2.hash(4, 4096, 1, pin.toCharArray());

            return hash;

            //byte[] combined = new byte[salt.length + hash.getBytes().length];

            //System.arraycopy(salt, 0, combined, 0, salt.length);
            //System.arraycopy(hash.getBytes(), 0, combined, salt.length, hash.getBytes().length);

            //return Base64.getEncoder().encodeToString(combined);
        }
        catch (Exception e) {
            throw new HashingException("Failed to hash Pin", e);
        }
    }

    @Override
    public boolean verifyPin(String pin, String hashedPin) throws HashingException {

        try {
        
           /*  byte[] combined = Base64.getDecoder().decode(hashedPin);

            byte[] salt = new byte[8];
            System.arraycopy(combined, 0, salt, 0, 8);

            byte[] storedHashBytes = new byte[combined.length - 8];
            System.arraycopy(combined, 8, storedHashBytes, 0, storedHashBytes.length);

            String storedHash = new String(storedHashBytes);
            */

            Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);

            return argon2.verify(hashedPin, pin.toCharArray());
        }
        catch (Exception e) {
            throw new HashingException("Failed to verify Pin", e);
        }
    }
}
