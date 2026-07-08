package com.safeNotes.repositories;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.junit.Test;

import com.safeNotes.models.domain.User;

public class UserRepositoryImplTest {

    @Test
    public void saveAndFindUserByUsername() throws Exception {
        Path tempHome = Files.createTempDirectory("safenotes-test");
        System.setProperty("user.home", tempHome.toString());

        UserRepository repository = new UserRepositoryImpl();
        User user = new User("user-1", "alice", new byte[] {1, 2, 3}, new byte[] {4, 5, 6});

        repository.save(user);

        Optional<User> persisted = repository.findByUsername("alice");
        assertTrue(persisted.isPresent());
        assertEquals("alice", persisted.get().getUsername());
        assertEquals("user-1", persisted.get().getUserId());
    }
}
