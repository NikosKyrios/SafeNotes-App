package com.safeNotes.repositories;

import java.util.List;
import java.util.Optional;

import com.safeNotes.exceptions.StorageException;
import com.safeNotes.models.domain.SecureNote;

public interface NoteRepository {
    
    void save(SecureNote note) throws StorageException;

    Optional<SecureNote> findById(String id) throws StorageException;

    List<SecureNote> findAll() throws StorageException;

    void update(SecureNote note) throws StorageException;

    void delete(String id) throws StorageException;

    boolean exists(String id) throws StorageException;

    List<SecureNote> findByOwner(String ownerId) throws StorageException;

}
