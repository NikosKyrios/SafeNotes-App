package com.safeNotes.models.dto;

import java.util.List;

import com.safeNotes.models.domain.TypingData;

public class RegistrationRequest {
    private String username;
    private String password;
    private boolean enableKeyStroke;
    private boolean enable2FA;
    private boolean enableLocationCheck;
    private List<TypingData> typingSamples;
    private String locationHash;
    private boolean autoLockEnabled;
    private int autoLockMins;

    public RegistrationRequest() {}

    public RegistrationRequest(String username, String password, boolean enableKeyStroke, boolean enable2FA, boolean enableLocationCheck, List<TypingData> typingSamples, String locationHash) {
        this.username = username;
        this.password = password;
        this.enableKeyStroke = enableKeyStroke;
        this.enable2FA = enable2FA;
        this.enableLocationCheck = enableLocationCheck;
        this.typingSamples = typingSamples;
        this.locationHash = locationHash;
    }

    public String getUsername() {return username;}
    public void setUsername(String username) {this.username = username;}

    public String getPassword() {return password;}
    public void setPassword(String password) {this.password = password;}

    public boolean isEnableKeyStroke() {return enableKeyStroke;}
    public void setEnableKeyStroke(boolean enableKeyStroke) {this.enableKeyStroke = enableKeyStroke;}

    public boolean isEnable2FA() {return enable2FA;}
    public void setEnable2FA(boolean enable2FA) {this.enable2FA = enable2FA;}

    public boolean isEnableLocationCheck() {return enableLocationCheck;}
    public void setEnableLocationCheck(boolean enableLocationCheck) {this.enableLocationCheck = enableLocationCheck;}

    public List<TypingData> getTypingSamples() {return typingSamples;}
    public void setTypingSamples(List<TypingData> typingSamples) {this.typingSamples = typingSamples;}

    public String getLocationHash() {return locationHash;}
    public void setLocationHash(String locationHash) {this.locationHash = locationHash;}

    public boolean isAutoLockEnabled() {return autoLockEnabled;}
    public void setAutoLockEnabled(boolean autoLockEnabled) {this.autoLockEnabled = autoLockEnabled;}

    public int getAutoLockMins() {return autoLockMins;}
    public void setAutoLockMins(int autoLockMins) {this.autoLockMins = autoLockMins;}

}
