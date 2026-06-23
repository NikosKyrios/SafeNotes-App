package com.safeNotes.config;

import java.util.Arrays;
import java.util.List;

public class SecurityConfig {
    public static final List<String> WEAK_PASSWORDS = Arrays.asList("password", "password2", "qwerty", "123456", "654321");

    public static boolean isPassowrdStrong(String password) {
        if (password.length() < AppConstants.MIN_PASSWORD_LENGTH) return false;
        if (WEAK_PASSWORDS.contains(password.toLowerCase())) return false;

        boolean hasUpper = !password.equals(password.toLowerCase());
        boolean hasLower = !password.equals(password.toUpperCase());
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");

        return hasUpper && hasLower && hasDigit && hasSpecial;
    }

    public static String getEncryptedVersion() {
        return AppConstants.ENCRYPTION_ALGORITHM;
    }
}
