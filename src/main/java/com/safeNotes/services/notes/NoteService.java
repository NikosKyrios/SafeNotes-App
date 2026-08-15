package com.safeNotes.services.notes;

import java.util.List;

import com.safeNotes.exceptions.HashingException;
import com.safeNotes.exceptions.NoteAccessException;
import com.safeNotes.exceptions.StorageException;
import com.safeNotes.models.domain.SecureNote;

public interface NoteService {
    
    SecureNote createNote(String title, String content, String ownerId) throws StorageException;

    List<SecureNote> getNotesByUser(String ownerid) throws StorageException;

    SecureNote getNoteById(String id, String userId) throws NoteAccessException, StorageException;

    SecureNote updateNote(String id, String title, String content, String userId) throws NoteAccessException, StorageException;

    void deleteNote(String id, String userId) throws NoteAccessException, StorageException;

    SecureNote lockNote(String id, String pin, String userId) throws NoteAccessException, StorageException;

    SecureNote unlockNote(String id, String pin, String userId) throws NoteAccessException, StorageException, HashingException;

    SecureNote toggleBlur(String id, String userId) throws NoteAccessException, StorageException;

    boolean verifyPin(String id, String pin, String userId) throws NoteAccessException, StorageException;
}
