package com.safeNotes.models.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class User {
    private String userId;
    private String username;
    private byte[] passwordHash;
    private byte[] salt;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
    private boolean twoFactoredEnabled;
    private String twoFactorSecret; //totp
    private TypingProfile typingProfile;
    private boolean requireKeystrokeAuth;
    private boolean autoLockEnabled;
    private int autoLockMins;
    private boolean locationCheckEnabled;
    private List<String> trustedLocationHashes;

    public User(String userId, String username, byte[] passwordHash, byte[] salt) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = Arrays.copyOf(passwordHash, passwordHash.length);
        this.salt = Arrays.copyOf(salt, salt.length);
        this.createdAt = LocalDateTime.now();
        this.lastLogin = null; // First login hasn't happened yet
        this.twoFactoredEnabled = false;
        this.twoFactorSecret = null;
        this.typingProfile = null;
        this.requireKeystrokeAuth = false;
        this.autoLockEnabled = false;
        this.autoLockMins = 15; // Default value
    }

    public User(String userId, String username, byte[] passwordHash, byte[] salt,
            LocalDateTime createdAt, boolean twoFactoredEnabled, 
            boolean requireKeystrokeAuth, boolean autoLockEnabled, int autoLockMins,
            boolean locationCheckEnabled, List<String> trustedLocationHashes) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = Arrays.copyOf(passwordHash, passwordHash.length);
        this.salt = Arrays.copyOf(salt, salt.length);
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.lastLogin = null;
        this.twoFactoredEnabled = twoFactoredEnabled;
        this.twoFactorSecret = null;
        this.typingProfile = null;
        this.requireKeystrokeAuth = requireKeystrokeAuth;
        this.autoLockEnabled = autoLockEnabled;
        this.autoLockMins = autoLockMins;
        this.locationCheckEnabled = locationCheckEnabled;
        this.trustedLocationHashes = trustedLocationHashes != null ? new ArrayList<>(trustedLocationHashes) : new ArrayList<>();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public byte[] getPasswordHash() {
        return passwordHash != null ? Arrays.copyOf(passwordHash, passwordHash.length) : null;
    }

    public void setPasswordHash(byte[] passwordHash) {
        if (this.passwordHash != null) {
            Arrays.fill(this.passwordHash, (byte) 0);
        }
        this.passwordHash = passwordHash != null ? 
            Arrays.copyOf(passwordHash, passwordHash.length) : null;
    }

    public byte[] getSalt() {
        return salt != null ? Arrays.copyOf(salt, salt.length) : null;
    }

    public void setSalt(byte[] salt) {
        if (this.salt != null) {
            Arrays.fill(this.salt, (byte) 0);
        }
        this.salt = salt != null ? Arrays.copyOf(salt, salt.length) : null;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public void updateLastLogin() {
        this.lastLogin = LocalDateTime.now();
    }

    public boolean isTwoFactoredEnabled() {
        return twoFactoredEnabled;
    }

    public void setTwoFactoredEnabled(boolean twoFactoredEnabled) {
        this.twoFactoredEnabled = twoFactoredEnabled;
    }

    public String getTwoFactorSecret() {
        return twoFactorSecret;
    }

    public void setTwoFactorSecret(String twoFactorSecret) {
        if (this.twoFactorSecret != null) {
            // Securely clear old secret
            char[] chars = this.twoFactorSecret.toCharArray();
            Arrays.fill(chars, '\0');
            this.twoFactorSecret = null;
        }
        this.twoFactorSecret = twoFactorSecret;
    }

    public TypingProfile getTypingProfile() {
        return typingProfile;
    }

    public void setTypingProfile(TypingProfile typingProfile) {
        this.typingProfile = typingProfile;
    }

    public boolean isRequireKeystrokeAuth() {
        return requireKeystrokeAuth;
    }

    public void setRequireKeystrokeAuth(boolean requireKeystrokeAuth) {
        this.requireKeystrokeAuth = requireKeystrokeAuth;
    }

    public boolean isAutoLockEnabled() {
        return autoLockEnabled;
    }

    public void setAutoLockEnabled(boolean autoLockEnabled) {
        this.autoLockEnabled = autoLockEnabled;
    }

    public int getAutoLockMins() {
        return autoLockMins;
    }

    public void setAutoLockMins(int autoLockMins) {
        if (autoLockMins < 1) {
            throw new IllegalArgumentException("Auto-lock minutes must be at least 1");
        }
        this.autoLockMins = autoLockMins;
    }

    public boolean isLocationCheckEnabled() {return locationCheckEnabled;}
    public void setLocationCheckEnabled(boolean locationCheckEnabled) {this.locationCheckEnabled = locationCheckEnabled;}

    public List<String> getTrustedLocationHashes() {return new ArrayList<>(trustedLocationHashes);}

    public void addTrustedLocation(String location) {this.trustedLocationHashes.add(location);}

    // Security-sensitive methods
    public void clearSensitiveData() {
        if (passwordHash != null) {
            Arrays.fill(passwordHash, (byte) 0);
            passwordHash = null;
        }
        if (salt != null) {
            Arrays.fill(salt, (byte) 0);
            salt = null;
        }
        if (twoFactorSecret != null) {
            char[] chars = twoFactorSecret.toCharArray();
            Arrays.fill(chars, '\0');
            twoFactorSecret = null;
        }
    }

    // Copy method for defensive programming
    public User copy() {
        User copy = new User(
            this.userId,
            this.username,
            this.passwordHash != null ? this.passwordHash.clone() : null,
            this.salt != null ? this.salt.clone() : null
        );
        copy.createdAt = this.createdAt;
        copy.lastLogin = this.lastLogin;
        copy.twoFactoredEnabled = this.twoFactoredEnabled;
        copy.twoFactorSecret = this.twoFactorSecret; // String is immutable
        copy.typingProfile = this.typingProfile;
        copy.requireKeystrokeAuth = this.requireKeystrokeAuth;
        copy.autoLockEnabled = this.autoLockEnabled;
        copy.autoLockMins = this.autoLockMins;
        return copy;
    }

    // Helper method to check if auto-lock is needed
    public boolean isAutoLockExpired(LocalDateTime lastActivity) {
        if (!autoLockEnabled || lastActivity == null) return false;
        return lastActivity.plusMinutes(autoLockMins).isBefore(LocalDateTime.now());
    }

    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", username='" + username + '\'' +
                ", createdAt=" + createdAt +
                ", twoFactoredEnabled=" + twoFactoredEnabled +
                '}';
    }

}
