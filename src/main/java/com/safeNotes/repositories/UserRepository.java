package com.safeNotes.repositories;

import java.util.List;

import com.safeNotes.exceptions.StorageException;
import com.safeNotes.models.domain.User;

public interface UserRepository {
    User save(User user) throws StorageException;
    User findByUsername(String username) throws StorageException;
    void delete(String userId) throws StorageException;
    boolean existsByUsername(String username) throws StorageException;
    List<User> findAll() throws StorageException;
}
