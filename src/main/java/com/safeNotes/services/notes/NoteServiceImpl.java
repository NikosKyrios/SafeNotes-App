package com.safeNotes.services.notes;

import java.util.List;


import java.util.Base64;

import com.safeNotes.exceptions.EncryptionException;
import com.safeNotes.exceptions.HashingException;
import com.safeNotes.exceptions.NoteAccessException;
import com.safeNotes.exceptions.StorageException;
import com.safeNotes.models.domain.SecureNote;
import com.safeNotes.repositories.NoteRepository;
import com.safeNotes.services.encryption.EncryptionService;
import com.safeNotes.services.encryption.PasswordHasher;

public class NoteServiceImpl implements NoteService {

    private final NoteRepository noteRepository;
    private final PasswordHasher hasher;
    private final EncryptionService encryptionService;
    private final byte[] encryptionKey;

    public NoteServiceImpl(NoteRepository noteRepository, PasswordHasher hasher, EncryptionService encryptionService, byte[] encryptionKey) {
        this.noteRepository = noteRepository;
        this.hasher = hasher;
        this.encryptionService = encryptionService;
        this.encryptionKey = encryptionKey;
    }

    @Override
    public SecureNote createNote(String title, String content, String ownerId) throws StorageException {
        if (title == null || title.trim().isEmpty()) {
            throw new StorageException("Note title cannot be empty");
        }

        SecureNote note = new SecureNote(title, content, ownerId);

        try {
            byte[] encrypted = encryptionService.encrypt(content, encryptionKey);
            note.setContent(Base64.getEncoder().encodeToString(encrypted));           
        } 
        catch (EncryptionException e) {
            throw new StorageException("Failed to encrypt note", e);
        }

        noteRepository.save(note);
        return note;
    }

    @Override
    public List<SecureNote> getNotesByUser(String ownerid) throws StorageException {
        if (ownerid == null || ownerid.trim().isEmpty()) {
            throw new StorageException("User Id cannot be empty");
        }
        return noteRepository.findByOwner(ownerid);
    }

    @Override
    public SecureNote getNoteById(String id, String userId) throws NoteAccessException, StorageException {
        SecureNote note = noteRepository.findById(id).orElseThrow(() -> new NoteAccessException("Note not found"));

        if (!note.getOwnerId().equals(userId)) {
            throw new NoteAccessException("You don't have permission to access this note");
        }

        try {
            byte[] encrypted = Base64.getDecoder().decode(note.getContent());
            String decrypted = encryptionService.decrypt(encrypted, encryptionKey);
            note.setContent(decrypted);
        }
        catch (EncryptionException e) {
            throw new StorageException("Failed to decrypt note", e);
        }

        return note;
    }

    @Override
    public SecureNote updateNote(String id, String title, String content, String userId)
            throws NoteAccessException, StorageException {
        SecureNote note = getNoteById(id, userId);
        note.setTitle(title);
        note.setContent(content);
        note.updateTimestamp();
        noteRepository.update(note);

        try {
            byte[] encrypted = encryptionService.encrypt(content, encryptionKey);
            note.setContent(Base64.getEncoder().encodeToString(encrypted));
        } 
        catch (EncryptionException e) {
            throw new StorageException("Failed to encrypt note", e);
        }

        return note;
    }

    @Override
    public void deleteNote(String id, String userId) throws NoteAccessException, StorageException {
        getNoteById(id, userId);
        noteRepository.delete(id);
    }

    @Override
    public SecureNote lockNote(String id, String pin, String userId) throws NoteAccessException, StorageException {
        if (pin == null || pin.length() < 4) {throw new StorageException("Pin must be at least 4 digits");}

        SecureNote note = getNoteById(id, userId);

        try {
            note.setPin(hasher.hashPin(pin));
        }
        catch (HashingException e) {
            throw new StorageException("Failed to hash Pin", e);
        }

        note.setLocked(true);
        note.updateTimestamp();
        noteRepository.update(note);
        return note;
    }

    @Override
    public SecureNote unlockNote(String id, String pin, String userId) throws NoteAccessException, StorageException {
        SecureNote note = getNoteById(id, userId);

        if (!note.isLocked()) {throw new StorageException("Note is not locked");}
        if (note.getPin() == null) {throw new StorageException("Note has no Pin");}

        try {
            if (!hasher.verifyPin(pin, note.getPin())) {throw new StorageException("Incorrect Pin");}
        }
        catch (HashingException e) {
            throw new StorageException("Failed to verify Pin", e);
        }

        note.setLocked(false);
        note.updateTimestamp();
        noteRepository.update(note);
        return note;
    }

    @Override
    public SecureNote toggleBlur(String id, String userId) throws NoteAccessException, StorageException {
        SecureNote note = getNoteById(id, userId);
        note.setBlurred(!note.isBlurred());
        note.updateTimestamp();
        noteRepository.update(note);
        return note;
    }

    @Override
    public boolean verifyPin(String id, String pin, String userId) throws NoteAccessException, StorageException {
        SecureNote note = getNoteById(id, userId);

        if (note.getPin() == null) {return false;}

        try {
            return hasher.verifyPin(pin, note.getPin());
        }
        catch (HashingException e) {
            throw new StorageException("Failed to verify Pin", e);
        }
    }
    
}
