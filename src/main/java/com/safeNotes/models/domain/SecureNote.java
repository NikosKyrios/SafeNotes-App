package com.safeNotes.models.domain;

import java.time.LocalDateTime;
import java.util.UUID;

public class SecureNote {

    private String id;
    private String title;
    private String content;
    private String ownerId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isLocked;
    private boolean isBlurred;
    private String pin;
    private String securityLevel;

    public SecureNote() {
        this.id = UUID.randomUUID().toString();
        this. createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.isLocked = false;
        this.isBlurred = false;
        this.securityLevel = "LOW";
    }

    public SecureNote(String title, String content, String ownerId) {
        this();
        this.title = title;
        this.content = content;
        this.ownerId = ownerId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean isLocked) {
        this.isLocked = isLocked;
    }

    public boolean isBlurred() {
        return isBlurred;
    }

    public void setBlurred(boolean isBlurred) {
        this.isBlurred = isBlurred;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getSecurityLevel() {
        return securityLevel;
    }

    public void setSecurityLevel(String securityLevel) {
        this.securityLevel = securityLevel;
    }
    
    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }

    public String getFormattedDate() {
        return createdAt.toLocalDate().toString();
    }
    
}
