package com.safeNotes.repositories;

import java.util.List;
import java.util.Optional;

import com.safeNotes.exceptions.StorageException;
import com.safeNotes.models.domain.User;

public interface UserRepository {
    void save(User user) throws StorageException;
    Optional<User> findByUsername(String username) throws StorageException;
    void delete(String userId) throws StorageException;
    boolean existsByUsername(String username) throws StorageException;
    List<User> findAll() throws StorageException;
}
