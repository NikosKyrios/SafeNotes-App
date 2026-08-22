package com.safeNotes.services.auth;

import java.util.List;

import com.safeNotes.exceptions.AuthException;
import com.safeNotes.models.dto.LoginResult;
import com.safeNotes.models.dto.RegistrationRequest;
import com.safeNotes.models.dto.RegistrationResult;

public interface AuthenticationService {
    LoginResult login(String username, String password, String locationHash) throws AuthException;

    RegistrationResult register(RegistrationRequest request) throws AuthException;

    void logout();

    boolean validatePasswordStrength(String password);
    boolean isUsernameAvailable(String username);
    void addTrusterLocation(String userId, String locationHash) throws AuthException;

    boolean verifyMasterPassword(String username, String password);

    List<String> getTrustedLocations(String id) throws AuthException;
    void addTrustedLocation(String userId, String locationHash) throws AuthException;
    void removeTrustedLocation(String userId, String locationHash) throws AuthException;

}
