package com.safeNotes.models.dto;

import com.safeNotes.models.domain.TypingData;

public class LoginRequest {
    private String username;
    private String password;
    private TypingData typingData;
    private String locationHash;

    public LoginRequest() {}

    public LoginRequest(String username, String password, TypingData typingData, String locationHash) {
        this.username = username;
        this.password = password;
        this.typingData = typingData;
        this.locationHash = locationHash;
    }

    public String getUsername() {return username;}
    public void setUsername(String username) {this.username = username;}

    public String getPassword() {return password;}
    public void setPassword(String password) {this.password = password;}

    public TypingData getTypingData() {return typingData;}
    public void setTypingData(TypingData typingData) {this.typingData = typingData;}

    public String getLocationHash() {return locationHash;}
    public void setLocationHash(String locationHash) {this.locationHash = locationHash;}
}
