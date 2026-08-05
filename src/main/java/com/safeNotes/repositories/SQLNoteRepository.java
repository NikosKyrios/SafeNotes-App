package com.safeNotes.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.safeNotes.config.DatabaseConfig;
import com.safeNotes.exceptions.StorageException;
import com.safeNotes.models.domain.SecureNote;

public class SQLNoteRepository implements NoteRepository {

    public SQLNoteRepository() throws StorageException {createTable();}

    private void createTable() throws StorageException {
        String sql = """
            CREATE TABLE IF NOT EXISTS notes (
                id TEXT PRIMARY KEY,
                title TEXT NOT NULL,
                content TEXT,
                owner_id TEXT NOT NULL,
                created_at TEXT NOT NULL,
                updated_at TEXT NOT NULL,
                is_locked INTEGER DEFAULT 0,
                is_blurred INTEGER DEFAULT 0,
                pin TEXT,
                security_level TEXT DEFAULT 'LOW'
            )
            """;
        try (Connection conn = DatabaseConfig.connect();
            Statement st = conn.createStatement()) {
            st.execute(sql);
        } catch (SQLException e) {
            throw new StorageException("Failed to create notes table", e);
        }
    }

    @Override
    public void save(SecureNote note) throws StorageException {
        String sql ="""
                INSERT INTO notes (id, title, content, owner_id, created_at, updated_at, is_locked, is_blurred, pin, security_level) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseConfig.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, note.getId());
            ps.setString(2, note.getTitle());
            ps.setString(3, note.getContent());
            ps.setString(4, note.getOwnerId());
            ps.setString(5, note.getCreatedAt().toString());
            ps.setString(6, note.getUpdatedAt().toString());
            ps.setInt(7, note.isLocked() ? 1 : 0);
            ps.setInt(8, note.isBlurred() ? 1 : 0);
            ps.setString(9, note.getPin());
            ps.setString(10, note.getSecurityLevel());
            ps.executeUpdate();
        } 
        catch (SQLException e) {
            throw new StorageException("Failed to save note: " + note.getTitle(), e);
        }
    }

    @Override
    public Optional<SecureNote> findById(String id) throws StorageException {
        String sql = "SELECT * FROM notes WHERE id = ?";

        try (Connection conn = DatabaseConfig.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {return Optional.of(mapNote(rs));}
             }
        catch (SQLException e) {
            throw new StorageException("Failed to find note: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public List<SecureNote> findAll() throws StorageException {
        String sql = "SELECT * FROM notes ORDER BY updated_at DESC";
        List<SecureNote> notes = new ArrayList<>();

        try (Connection conn = DatabaseConfig.connect();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                notes.add(mapNote(rs));
            }
        } catch (SQLException e) {
            throw new StorageException("Failed to load all notes", e);
        }
        return notes;
    }

    @Override
    public void update(SecureNote note) throws StorageException {
        String sql = """
                UPDATE notes SET                 
                title = ?, content = ?, updated_at = ?, 
                is_locked = ?, is_blurred = ?, pin = ?, security_level = ?
                WHERE id = ?
                """;

        try (Connection conn = DatabaseConfig.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, note.getTitle());
            ps.setString(2, note.getContent());
            ps.setString(3, note.getUpdatedAt().toString());
            ps.setInt(4, note.isLocked() ? 1 : 0);
            ps.setInt(5, note.isBlurred() ? 1 : 0);
            ps.setString(6, note.getPin());
            ps.setString(7, note.getSecurityLevel());
            ps.setString(8, note.getId());
            ps.executeUpdate();
        } 
        catch (SQLException e) {
            throw new StorageException("Failed to update note: " + note.getId(), e);
        }
    }

    @Override
    public void delete(String id) throws StorageException {
        String sql = "DELETE FROM notes WHERE id = ?";

        try (Connection conn = DatabaseConfig.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new StorageException("Failed to delete note: " + id, e);
        }
    }

    @Override
    public boolean exists(String id) throws StorageException {
        String sql = "SELECT COUNT(*) FROM notes WHERE id = ?";

        try (Connection conn = DatabaseConfig.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            throw new StorageException("Failed to check note existence: " + id, e);
        }
    }

    public List<SecureNote> findByOwner(String ownerId) throws StorageException {
        String sql = "SELECT * FROM notes WHERE owner_id = ? ORDER BY updated_at DESC";
        List<SecureNote> notes = new ArrayList<>();

        try (Connection conn = DatabaseConfig.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ownerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                notes.add(mapNote(rs));
            }
        } catch (SQLException e) {
            throw new StorageException("Failed to find notes for owner: " + ownerId, e);
        }
        return notes;
    }

    private SecureNote mapNote(ResultSet rs) throws SQLException {
        SecureNote note = new SecureNote();
        note.setId(rs.getString("id"));
        note.setTitle(rs.getString("title"));
        note.setContent(rs.getString("content"));
        note.setOwnerId(rs.getString("owner_id"));
        note.setCreatedAt(LocalDateTime.parse(rs.getString("created_at")));
        note.setUpdatedAt(LocalDateTime.parse(rs.getString("updated_at")));
        note.setLocked(rs.getInt("is_locked") == 1);
        note.setBlurred(rs.getInt("is_blurred") == 1);
        note.setPin(rs.getString("pin"));
        note.setSecurityLevel(rs.getString("security_level"));
        return note;
    }

}