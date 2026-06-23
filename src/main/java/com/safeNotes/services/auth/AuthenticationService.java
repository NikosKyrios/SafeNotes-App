package com.safeNotes.services.auth;

import com.safeNotes.exceptions.AuthException;
import com.safeNotes.models.dto.LoginRequest;
import com.safeNotes.models.dto.LoginResult;
import com.safeNotes.models.dto.RegistrationRequest;
import com.safeNotes.models.dto.RegistrationResult;

public interface AuthenticationService {
    LoginResult login(LoginRequest request) throws AuthException;

    RegistrationResult register(RegistrationRequest request) throws AuthException;

    void logout();

    boolean validatePasswordStrength(String password);
    boolean isUsernameAvailable(String username);
    void addTrusterLocation(String userId, String locationHash) throws AuthException;
}
