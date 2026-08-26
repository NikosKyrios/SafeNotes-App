package com.safeNotes.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.safeNotes.config.DatabaseConfig;
import com.safeNotes.exceptions.StorageException;
import com.safeNotes.models.domain.User;

public class SQLUserRepository implements UserRepository {

    public SQLUserRepository() throws StorageException {
        createTable();
    }

    private void createTable() throws StorageException {
        String sql = """
            CREATE TABLE IF NOT EXISTS users (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            user_id TEXT UNIQUE NOT NULL,
            username TEXT UNIQUE NOT NULL,
            password_hash BLOB NOT NULL,
            salt BLOB NOT NULL,
            created_at TEXT NOT NULL,
            last_login TEXT,
            trusted_locations TEXT,
            location_check_enabled INTEGER DEFAULT 0
            )
            """;
        try (Connection conn = DatabaseConfig.connect();
             Statement st = conn.createStatement()) {
            st.execute(sql);
        }
        catch (SQLException e) {
            throw new StorageException("Failed to create users table", e);
        }
    }

    @Override
    public void save(User user) throws StorageException {
        String sql = """
            INSERT INTO users (user_id, username, password_hash, salt, created_at, last_login, trusted_locations, location_check_enabled)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT(username) DO UPDATE SET
                user_id = excluded.user_id,
                password_hash = excluded.password_hash,
                salt = excluded.salt,
                created_at = excluded.created_at,
                last_login = excluded.last_login,
                trusted_locations = excluded.trusted_locations,
                location_check_enabled = excluded.location_check_enabled
            """;

        try (Connection conn = DatabaseConfig.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            byte[] passwordHash = user.getPasswordHash() != null ? user.getPasswordHash() : new byte[0];
            byte[] salt = user.getSalt() != null ? user.getSalt() : new byte[0];

            String trustedLocationsJSON = convertToJson(user.getTrustedLocationMap());

            ps.setString(1, user.getUserId());
            ps.setString(2, user.getUsername());
            ps.setBytes(3, passwordHash);
            ps.setBytes(4, salt);
            ps.setString(5, user.getCreatedAt().toString());
            ps.setString(6, user.getLastLogin() != null ? user.getLastLogin().toString() : null);
            ps.setString(7, trustedLocationsJSON);
            ps.setInt(8, user.isLocationCheckEnabled() ? 1 : 0);
            ps.executeUpdate();
        }
        catch (SQLException e) {
            throw new StorageException("Failed to save user: " + user.getUsername(), e);
        }
    }

    @Override
    public Optional<User> findByUsername(String username) throws StorageException {
        String sql = "SELECT user_id, username, password_hash, salt, created_at, last_login, trusted_locations, location_check_enabled FROM users WHERE username = ?";

        try (Connection conn = DatabaseConfig.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapUser(rs));
                }
            }
        }
        catch (SQLException e) {
            throw new StorageException("Failed to find user: " + username, e);
        }
        return Optional.empty();
    }

    @Override
    public void delete(String userId) throws StorageException {
        String sql = "DELETE FROM users WHERE user_id = ? OR username = ?";

        try (Connection conn = DatabaseConfig.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setString(2, userId);
            ps.executeUpdate();
        }
        catch (SQLException e) {
            throw new StorageException("Failed to delete user: " + userId, e);
        }
    }

    @Override
    public boolean existsByUsername(String username) throws StorageException {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";

        try (Connection conn = DatabaseConfig.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
        catch (SQLException e) {
            throw new StorageException("Failed to check username: " + username, e);
        }
    }

    @Override
    public List<User> findAll() throws StorageException {
        String sql = "SELECT user_id, username, password_hash, salt, created_at, last_login, trusted_locations, location_check_enabled FROM users";
        List<User> users = new ArrayList<>();

        try (Connection conn = DatabaseConfig.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                users.add(mapUser(rs));
            }
        }
        catch (SQLException e) {
            throw new StorageException("Failed to load users", e);
        }
        return users;
    }

    private User mapUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getString("user_id"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getBytes("password_hash"));
        user.setSalt(rs.getBytes("salt"));
        user.setCreatedAt(LocalDateTime.parse(rs.getString("created_at")));

        String trustedJson = rs.getString("trusted_locations");
        if (trustedJson != null && !trustedJson.isEmpty()) {
            Map<String, String> trustedMap = parseJsonToMap(trustedJson);
            for (Map.Entry<String, String> entry : trustedMap.entrySet()) {
                user.addTrustedLocation(entry.getKey());
            }
        }
        user.setLocationCheckEnabled(rs.getInt("location_check_enabled") == 1);

        String lastLogin = rs.getString("last_login");
        if (lastLogin != null) {
            user.setLastLogin(LocalDateTime.parse(lastLogin));
        }
        return user;
    }

    private String convertToJson(Map<String, String> map) {
        if (map == null || map.isEmpty()) return "{}";
        StringBuilder sb = new StringBuilder("{");
        int i = 0;

        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(entry.getKey()).append("\":\"").append(entry.getValue()).append("\"");
            i++;
        }
        sb.append("}");
        return sb.toString();
    }

    private Map<String, String> parseJsonToMap(String json) {
        Map<String, String> map = new HashMap<>();
        if (json == null || json.equals("{}")) return map;
        try {
            // Remove { } and split by comma
            json = json.substring(1, json.length() - 1);
            if (json.isEmpty()) return map;
            String[] pairs = json.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
            for (String pair : pairs) {
                String[] keyValue = pair.split(":(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                if (keyValue.length == 2) {
                    String key = keyValue[0].replace("\"", "").trim();
                    String value = keyValue[1].replace("\"", "").trim();
                    map.put(key, value);
                }
            }
        } catch (Exception e) {
            // Return empty map on error
        }
        return map;
    }
    
}
