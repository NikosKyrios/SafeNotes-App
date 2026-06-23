package com.safeNotes.services.auth;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.safeNotes.exceptions.AuthException;
import com.safeNotes.exceptions.HashingException;
import com.safeNotes.exceptions.StorageException;
import com.safeNotes.models.domain.TypingData;
import com.safeNotes.models.domain.TypingProfile;
import com.safeNotes.models.domain.User;
import com.safeNotes.models.dto.LoginRequest;
import com.safeNotes.models.dto.LoginResult;
import com.safeNotes.models.dto.RegistrationRequest;
import com.safeNotes.models.dto.RegistrationResult;
import com.safeNotes.repositories.UserRepository;
import com.safeNotes.services.encryption.PasswordHasher;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;

public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final SessionManager sessionManager;

    public AuthenticationServiceImpl(UserRepository userRepository, PasswordHasher passwordHasher,
            SessionManager sessionManager) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.sessionManager = sessionManager;
    }

    private final Map<String, Integer> locationFailures = new ConcurrentHashMap<>();
    private final Map<String, Long> blockedAccounts = new ConcurrentHashMap<>();

    @Override
    public LoginResult login(LoginRequest request) throws AuthException {
        String username = request.getUsername();

        //1. Check if acc is blocked
        if (isAccountBlocked(username)) {
            throw new AuthException("Account is locked. Try again after 24h");
        }

        //2. Find user
        User user;
        try {
            user = userRepository.findByUsername(username);
        }
        catch (StorageException e) {
            throw new AuthException("Database error. Please try again.", e);
        }
        if (user == null) {
            throw new AuthException("Invalid Username or Password");
        }

        //3. Verify Password
        boolean passwordValid;
        try {
            passwordValid = passwordHasher.verify(request.getPassword(), user.getPasswordHash());
        }
        catch (HashingException e) {
            throw new AuthException("Authentication Error. Please try again.", e);
        }
        if (!passwordValid) {throw new AuthException("Invalid username or password");}

        //4. KeyStroke verification
        if (user.isRequireKeystrokeAuth()) {
            boolean keyStrokeValid = verifyKeyStroke(user, request.getTypingData());
            if (!keyStrokeValid) {
                throw new AuthException("KeyStroke pattern does not match");
            }
        }
        //5. Location verification
        if (user.isLocationCheckEnabled()) {
            boolean locationValid = verifyLocation(user, request.getLocationHash());
            if (!locationValid) {
                trackLocationFailure(username);
                throw new AuthException("Untrusted location. Access denied");
            }
            locationFailures.remove(username);
        }

        //6. Update last login
        user.updateLastLogin();
        try {
            userRepository.save(user);
        }
        catch (StorageException e) {
            throw new AuthException("Failed to update last login", e);
        }

        //7. Create session
        String sessionId = sessionManager.createSession(user);

        return new LoginResult(true, false, user.getUserId(), sessionId, "Login successfull", null);
    }

    @Override
    public void addTrusterLocation(String userId, String locationHash) throws AuthException {
        User user;
        try {
            user = userRepository.findByUsername(userId);
        }
        catch (StorageException e) {
            throw new AuthException("Location not found");
        }
        if (user == null) {throw new AuthException("User not found");}

        user.addTrustedLocation(locationHash);

        try {
            userRepository.save(user);
        } 
        catch (StorageException e) {
            throw new AuthException("Failed to save trusted location", e);
        }
    }

    @Override
    public boolean isUsernameAvailable(String username) {
        try {
            return !userRepository.existsByUsername(username);
        }
        catch (StorageException e) {
            return false;
        }
    }

    @Override
    public void logout() {
        sessionManager.logout();
    }

    @Override
    public RegistrationResult register(RegistrationRequest request) throws AuthException {
        String username = request.getUsername();

        //1. Check if username exists
        try {
            if (userRepository.existsByUsername(username)) {throw new AuthException("Username already exists");}
        }
        catch (StorageException e) {
            throw new AuthException("Database Error. Please try again.", e);
        }

        //2. Validate password strength
        if (!validatePasswordStrength(request.getPassword())) {
            throw new AuthException("Password does not meet requirements.");
        }

        //3. Hash Password
        byte[] passwordHash;
        try {
            passwordHash = passwordHasher.hashPassword(request.getPassword());
        }
        catch (HashingException e) {
            throw new AuthException("Failed to hash Password", e);
        }

        //4. Create user
        String userId = UUID.randomUUID().toString();
        User user = new User(userId, username, passwordHash, null);

        //5. Preferences
        user.setRequireKeystrokeAuth(request.isEnableKeyStroke());
        user.setTwoFactoredEnabled(request.isEnable2FA());
        user.setLocationCheckEnabled(request.isEnableLocationCheck());
        
        //6. Setup keystroke
        if (request.isEnableKeyStroke() && request.getTypingSamples() != null) {
            TypingProfile profile = new TypingProfile(userId);
            for (TypingData sample : request.getTypingSamples()) {
                profile.addSample(sample);
            }
            user.setTypingProfile(profile);
        }

        //7. Add trusted location
        if (request.isEnableLocationCheck() && request.getLocationHash() != null) {
            user.addTrustedLocation(request.getLocationHash());
        }

        //8. Save user
        try {
            userRepository.save(user);
        }
        catch (StorageException e) {
            throw new AuthException("Failed to save user.", e);
        }

        //9. Return
        String QRURL = request.isEnable2FA() ? generateQRUrl(user) : null;
        return new RegistrationResult(true, userId, "Registration successfull", QRURL, 0);
    }

    @Override
    public boolean validatePasswordStrength(String password) {
        boolean lengthOk = password.length() >= 10;
        boolean hasUpper = !password.equals(password.toLowerCase());
        boolean hasLower = !password.equals(password.toUpperCase());
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");

        return lengthOk && hasUpper && hasLower && hasDigit && hasSpecial;
    }

    //Helper functions

    private boolean isAccountBlocked(String username) {
        Long unlockTime = blockedAccounts.get(username);
        if (unlockTime == null) return false;

        if (System.currentTimeMillis() > unlockTime) {
            blockedAccounts.remove(username);
            return false;
        }
        return true;
    }

    private boolean verifyKeyStroke(User user, TypingData typingData) {
        if (typingData == null) return false;
        if (typingData.isCopyPaste()) return false;

        TypingProfile profile = user.getTypingProfile();
        if (profile == null) return false;
        if (!profile.isCalibrationEnded()) return true;

        return profile.matches(typingData);
    }

    private boolean verifyLocation(User user, String locationHash) {
        if (locationHash == null) return false;
        return user.getTrustedLocationHashes().contains(locationHash);
    }

    private void trackLocationFailure(String username) {
        int failures = locationFailures.getOrDefault(username, 0) + 1;
        locationFailures.put(username, failures);

        if (failures >= 2) {
            blockedAccounts.put(username, System.currentTimeMillis() + (24 * 60 * 60 * 1000));
            locationFailures.remove(username);
        }
    }

    private String generateQRUrl(User user) {
        GoogleAuthenticator gglAuth = new GoogleAuthenticator();
        GoogleAuthenticatorKey key = gglAuth.createCredentials();

        String secret = key.getKey();
        user.setTwoFactorSecret(secret);

        String issuer = "SafeNotes";
        String QRURL = String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s", issuer, user.getUsername(), secret, issuer);

        return QRURL;
    }
    
}
