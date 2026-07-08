package com.safeNotes.app;

import com.safeNotes.exceptions.AuthException;
import com.safeNotes.exceptions.StorageException;
import com.safeNotes.models.dto.LoginRequest;
import com.safeNotes.models.dto.LoginResult;
import com.safeNotes.models.dto.RegistrationRequest;
import com.safeNotes.models.dto.RegistrationResult;
import com.safeNotes.repositories.SQLUserRepository;
import com.safeNotes.repositories.UserRepository;
import com.safeNotes.services.auth.AuthenticationService;
import com.safeNotes.services.auth.AuthenticationServiceImpl;
import com.safeNotes.services.auth.SessionManager;
import com.safeNotes.services.encryption.Argon2Hasher;
import com.safeNotes.services.encryption.PasswordHasher;

public class Main {
    public static void main(String[] args) throws StorageException {
        PasswordHasher hasher = new Argon2Hasher();
        UserRepository repo = new SQLUserRepository();
        SessionManager sessionManager = new SessionManager();
        AuthenticationService auth = new AuthenticationServiceImpl(repo, hasher, sessionManager);

        String username = "testuser12";
        String password = "SecurePassword12!";

        RegistrationRequest registrationRequest = new RegistrationRequest(username, password, false, false, false, null, null);
        RegistrationResult registrationResult;
        try {
            registrationResult = auth.register(registrationRequest);
            System.out.println("Registration: " + registrationResult.isSuccess() + " - " + registrationResult.getMessage());
        } catch (AuthException e) {
            System.out.println("Registration failed: " + e.getMessage());
            return;
        }

        LoginRequest loginRequest = new LoginRequest(username, password, null, null);
        LoginResult loginResult;
        try {
            loginResult = auth.login(loginRequest);
            System.out.println("Login: " + loginResult.isSuccess() + " - " + loginResult.getMessage());
        } catch (AuthException e) {
            System.out.println("Login failed: " + e.getMessage());
        }
    }
}
