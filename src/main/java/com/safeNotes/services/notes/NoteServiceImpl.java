package com.safeNotes.services.notes;

import java.util.List;

import com.safeNotes.exceptions.HashingException;
import com.safeNotes.exceptions.NoteAccessException;
import com.safeNotes.exceptions.StorageException;
import com.safeNotes.models.domain.SecureNote;
import com.safeNotes.repositories.NoteRepository;
import com.safeNotes.services.encryption.PasswordHasher;

public class NoteServiceImpl implements NoteService {

    private final NoteRepository noteRepository;
    private final PasswordHasher hasher;

    public NoteServiceImpl(NoteRepository noteRepository, PasswordHasher hasher) {
        this.noteRepository = noteRepository;
        this.hasher = hasher;
    }

    @Override
    public SecureNote createNote(String title, String content, String ownerId) throws StorageException {
        if (title == null || title.trim().isEmpty()) {
            throw new StorageException("Note title cannot be empty");
        }

        SecureNote note = new SecureNote(title, content, ownerId);
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
            String hashedPin = hasher.hashPin(pin);
            note.setPin(hashedPin);
        }
        catch (HashingException e) {
            throw new StorageException("Failed to hash Pin", e);
        }

        note.setPin(pin);
        note.setLocked(true);
        note.updateTimestamp();
        noteRepository.update(note);
        return note;
    }

    @Override
    public SecureNote unlockNote(String id, String pin, String userId) throws NoteAccessException, StorageException {
        SecureNote note = getNoteById(id, userId);

        if (!note.isLocked()) {throw new StorageException("Note is not locked");}
        if (!pin.equals(note.getPin())) {throw new StorageException("Incorrect Pin");}
        if (note.getPin() == null) {throw new StorageException("Note has no Pin");}

        try {
            boolean pinValid = hasher.verifyPin(pin, note.getPin());
            if (!pinValid) {throw new StorageException("Incorrect Pin");}
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
