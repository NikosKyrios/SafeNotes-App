package com.safeNotes.models.dto;

import java.util.List;

import com.safeNotes.models.domain.TypingData;

public class RegistrationResult {
    private boolean success;
    private String userId;
    private String message;
    private String twoFactorQRUrl;
    private int remainingSamples;

    public RegistrationResult() {}

    public RegistrationResult(boolean success, String userId, String message, String twoFactorQRUrl, int remainingSamples) {
        this.success = success;
        this.userId = userId;
        this.message = message;
        this.twoFactorQRUrl = twoFactorQRUrl;
        this.remainingSamples = remainingSamples;
    }

    public boolean isSuccess() {return success;}
    public void setSuccess(boolean success) {this.success = success;}

    public String getUserId() {return userId;}
    public void setUserId(String userId) {this.userId = userId;}

    public String getMessage() {return message;}
    public void setMessage(String message) {this.message = message;}

    public String getTwoFactorQRUrl() {return twoFactorQRUrl;}
    public void setTwoFactorQRUrl(String twoFactorQRUrl) {this.twoFactorQRUrl = twoFactorQRUrl;}

    public int getRemainingSamples() {return remainingSamples;}
    public void setRemainingSamples(int remainingSamples) {this.remainingSamples = remainingSamples;}
}
