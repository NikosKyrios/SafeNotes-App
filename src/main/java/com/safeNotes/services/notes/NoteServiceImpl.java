package com.safeNotes.services.notes;

import java.util.List;

import com.safeNotes.exceptions.NoteAccessException;
import com.safeNotes.exceptions.StorageException;
import com.safeNotes.models.domain.SecureNote;
import com.safeNotes.repositories.NoteRepository;

public class NoteServiceImpl implements NoteService {

    private final NoteRepository noteRepository;

    public NoteServiceImpl(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
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
        //TODO: Hash the Pin
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

        //TODO: Verify hashed pin

        if (!pin.equals(note.getPin())) {throw new StorageException("Incorrect Pin");}

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
        //TODO: Compare hashed pin

        return pin != null && pin.equals(note.getPin());
    }
    
}
