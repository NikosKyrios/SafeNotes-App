package com.safeNotes.services.auth;

import java.util.List;
import java.util.Map;

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

    boolean verifyMasterPassword(String username, String password);

    List<String> getTrustedLocations(String username) throws AuthException;
    void addTrustedLocation(String username, String ip) throws AuthException;
    void removeTrustedLocation(String username, String ip) throws AuthException;

    List<String> getTrustedLocationHashes(String username) throws AuthException;
    Map<String, String> getTrustedLocationMap(String username) throws AuthException;

}
