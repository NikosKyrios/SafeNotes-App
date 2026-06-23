# Architecture Overview

## Core Components:
###
- Authentication System (Login/Register)

- User Profile (Security preferences)

- Notes Manager (CRUD operations)

- Security Layer (Optional features per note)

- Encryption Engine (Data protection)

###

### Keystroke Dynamics Authentication
IMPLEMENTATION:
- First 2 entries: Establish baseline (average typing speed)
- Calculate: Mean + Standard Deviation
- Adaptive threshold: ±1.6s → ±1.4s → ±1.2s → ±1.0s
- Track from first keypress to Enter key
- Store as hash pattern, not raw timing
- Allow 3 failed attempts before fallback authentication

SECURITY CONSIDERATIONS:
- Store pattern locally, not on server
- Use machine learning for pattern recognition
- Include backspace timing patterns
- Consider environmental factors (keyboard type)

### Multi-Layered Note Security
PER-NOTE OPTIONS:
- PIN Protection (4-8 digit)
- Password Protection (separate from login)
- Blur/Steganography Mode
- Self-destruct timer (after X views or time)
- Decoy notes (fake content if wrong PIN)
- Location-based access (only opens in specific places)
- Time-lock (only accessible at certain times)

### Blurred/Steganography Notes
ENHANCED IMPLEMENTATION:
- Command interface: `/show [note_name] [pin]`
- Multiple blur levels: Light, Medium, Full
- Fake blur option (shows different content)
- Decoy notes that log access attempts
- Optional: Image-based steganography (hide text in images)

## Additional Security Features to Consider

### Biometric Fallback
- Fingerprint/face recognition as backup
- Webcam-based blink pattern verification
- Voice pattern for emergency access

### Behavioral Analytics
- Monitor typical usage patterns (time of day, note access frequency)
- Flag anomalous behavior (accessing all notes at once)
- Silent alarm trigger (enters "safe mode" with fake data)

### Encryption
LAYERED ENCRYPTION:
1. Application-level: AES-256 for all notes
2. Per-note encryption with unique key
3. Optional: User-provided passphrase for double encryption
4. Master key encrypted with user's password

KEY MANAGEMENT:
- Never store plaintext passwords
- Use PBKDF2 or Argon2 for key derivation
- Implement secure key rotation

### Convert Features
- Panic Mode: Ctrl+Shift+Q shows only decoy notes
- Duel PINs: One PIN shows real notes, another shows fake notes
- Geofencing: Notes auto-lock outside safe locations
- Network Lock: Disable if connected to unknown WiFi

### Audit & Monitoring
- Log all access attempts (success/failure)
- Note viewing history with timestamps
- Exportable security report
- Suspicious activity alerts

## Technical Implementation Roadmap

### Phase 1: Core Application
1. JavaFX GUI with Material Design
2. SQLite database (local) or Firebase (cloud)
3. Basic authentication system
4. Note CRUD operations
5. Basic encryption

### Phase 2: Security Features
1. Implement keystroke dynamics
2. Add per-note PIN system
3. Create blur/command system
4. Implement 2FA (TOTP via Google Authenticator)

### Phase 3: Advanced Features
1. Behavioral analytics
2. Covert modes
3. Cloud sync with end-to-end encryption
4. Mobile companion app


### Access Control Matrix
USER → NOTE SECURITY MATRIX:
- Owner: Full access (modify security settings)
- Shared View: Read-only with same security
- Emergency Contact: Limited access after delay
- Intruder: Decoy content with logging

## Potential Challenges & Solutions

- Challenge 1: False Positives in Keystroke Dynamics

        Solution: Implement learning algorithm that adjusts over time, with SMS/email fallback.

- Challenge 2: User Experience vs Security

        Solution: Progressive disclosure - start simple, add features as needed. Clear visual indicators of security level.

- Challenge 3: Data Recovery

        Solution:

        Encrypted backup to cloud

        Recovery keys printed/offline storage

        Trusted contacts recovery system

- Challenge 4: Performance

        Solution:

        Lazy loading of encrypted notes

        Background encryption/decryption

        Cache frequently accessed notes in memory (encrypted)

## Additional Feature Ideas

- Security Scoring System

        Each note gets a "security score" based on features used

        Recommendations for improving security

        Visual indicators (padlock icons with different colors)

- Shared Notes with Granular Permissions

        Share notes with others

        Set expiration on shared access

        Watermark shared notes with viewer's identity

- Automated Threat Response

        If multiple failed attempts detected:

        Take screenshot (with user permission)

        Log IP address

        Send alert to email

        Temporarily lock account

        Present decoy data

- Steganography Advanced

        Hide notes in:

        Image files (LSB technique)

        Audio files (spectral hiding)

        Text files (whitespace/steganography)

        PDF metadata

## Development Stack Suggestions
- Frontend: JavaFX (as planned) or Electron for cross-platform
- Backend: Spring Boot (optional, for sync)
- Database: SQLite (local), PostgreSQL (server)
- Encryption: BouncyCastle library for Java
- 2FA: TOTP implementation
- Biometrics: Java Native Access for system biometrics