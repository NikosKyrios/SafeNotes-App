package com.safeNotes.models.dto;

public class LoginResult {
    private boolean success;
    private boolean requires2FA;
    private String userId;
    private String sessionId;
    private String message;
    private String twoFactorQRUrl;

    public LoginResult() {}

    public LoginResult(boolean success, boolean requires2FA, String userId, String sessionId, String message, String twoFactorQRUrl) {
        this.success = success;
        this.requires2FA = requires2FA;
        this.userId = userId;
        this.sessionId = sessionId;
        this.message = message;
        this.twoFactorQRUrl = twoFactorQRUrl;
    }

    public boolean isSuccess() {return success;}
    public void setSuccess(boolean success) {this.success = success;}

    public boolean isRequires2FA() {return requires2FA;}
    public void setRequires2FA(boolean requires2FA) {this.requires2FA = requires2FA;}

    public String getUserId() {return userId;}
    public void setUserId(String userId) {this.userId = userId;}

    public String getSessionId() {return sessionId;}
    public void setSessionId(String sessionId) {this.sessionId = sessionId;}

    public String getMessage() {return message;}
    public void setMessage(String message) {this.message = message;}

    public String getTwoFactorQRUrl() {return twoFactorQRUrl;}
    public void setTwoFactorQRUrl(String twoFactorQRUrl) {this.twoFactorQRUrl = twoFactorQRUrl;}
}
