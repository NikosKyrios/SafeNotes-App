package com.safeNotes.repositories;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.safeNotes.config.AppConstants;
import com.safeNotes.exceptions.StorageException;
import com.safeNotes.models.domain.User;

public class UserRepositoryImpl implements UserRepository {

    private final Map<String, User> usersByUsername = new ConcurrentHashMap<>();
    private final Map<String, User> usersById = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;
    private final Path dataFile;

    public UserRepositoryImpl() throws StorageException {
        this.objectMapper = new ObjectMapper();
        this.dataFile = Paths.get(AppConstants.USERS_FILE);
        loadFromFile();
    }

    @Override
    public void delete(String userId) throws StorageException {
        User user = usersById.get(userId);
        if (user != null) {
            usersByUsername.remove(user.getUsername());
            usersById.remove(userId);
            saveToFile();
        }
    }

    @Override
    public boolean existsByUsername(String username) throws StorageException {
        return usersByUsername.containsKey(username);
    }

    @Override
    public List<User> findAll() throws StorageException {
        List<User> result = new ArrayList<>();
        for (User user : usersByUsername.values()) {
            result.add(user.copy());
        }
        return result;
    }

    @Override
    public Optional<User> findByUsername(String username) throws StorageException {
        User user = usersByUsername.get(username);
        return Optional.ofNullable(user).map(User::copy);
    }

    @Override
    public void save(User user) throws StorageException {
        if (user == null) {
            throw new StorageException("User can not be null");
        }

        User copy = user.copy();
        usersByUsername.put(copy.getUsername(), copy);
        if (copy.getUserId() != null) {
            usersById.put(copy.getUserId(), copy);
        }
        saveToFile();
    }

    private void loadFromFile() throws StorageException {
        if (!Files.exists(dataFile)) {
            return;
        }

        try {
            @SuppressWarnings("unchecked")
            Map<String, User> loadedUsers = objectMapper.readValue(dataFile.toFile(), Map.class);

            usersByUsername.clear();
            usersById.clear();

            for (Map.Entry<String, User> entry : loadedUsers.entrySet()) {
                User user = entry.getValue();
                usersByUsername.put(user.getUsername(), user);
                if (user.getUserId() != null) {
                    usersById.put(user.getUserId(), user);
                }
            }
        }
        catch (IOException e) {
            throw new StorageException("Failed to load users from file", e);
        }
    }

    private void saveToFile() throws StorageException {
        try {
            Files.createDirectories(dataFile.getParent());
            Path tempFile = Files.createTempFile(dataFile.getParent(), "users", ".tmp");

            objectMapper.writerWithDefaultPrettyPrinter().writeValue(tempFile.toFile(), usersByUsername);

            Files.move(tempFile, dataFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        }
        catch (IOException e) {
            throw new StorageException("Failed to save users", e);
        }
    }
}
