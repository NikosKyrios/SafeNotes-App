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
        SafeNotesApp.launch(SafeNotesApp.class, args);
    } 
}
