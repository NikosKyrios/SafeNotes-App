package com.safeNotes.models.domain;

import java.util.ArrayList;
import java.util.List;

public class TypingProfile {
    private String userId;
    private List<Long> typingSamples;
    private long averageTypingTime;
    private long threshold;
    private boolean calibrationEnded;
    private int requiredSamples;

    public TypingProfile() {
        this.typingSamples = new ArrayList<>();
        this.requiredSamples = 3;
        this.threshold = 800;
        this.calibrationEnded = false;
    }

    public TypingProfile(String userId) {
        this();
        this.userId = userId;
    }

    public void addSample(TypingData sample) {
        if (!sample.isCopyPaste()) {
            typingSamples.add(sample.getTotalTypingTime());
            updateAverage();

            if (typingSamples.size() >= requiredSamples) {
                calibrationEnded = true;
            }
        }
    }

    private void updateAverage() {
        long sum = 0;
        for (Long time : typingSamples) {
            sum += time;
        }
        this.averageTypingTime = sum / typingSamples.size();
    }

    public boolean matches(TypingData loginAttempt) {
        if (!calibrationEnded) return true;
        if (loginAttempt.isCopyPaste()) return false;

        long difference = Math.abs(loginAttempt.getTotalTypingTime() - averageTypingTime);

        return difference <= threshold;
    }

    public String getUserId() {return userId;}
    public void setUserId(String userId) {this.userId = userId;}

    public List<Long> getTypingSamples() {return typingSamples;}
    public void setTypingSamples(List<Long> typingSamples) {this.typingSamples = typingSamples;}

    public long getAverageTypingTime() {return averageTypingTime;}

    public long getThreshold() {return threshold;}
    public void setThreshold(long threshold) {this.threshold = threshold;}

    public boolean isCalibrationEnded() {return calibrationEnded;}
    public void setCalibrationEnded(boolean calibrationEnded) {this.calibrationEnded = calibrationEnded;}

    public int getRequiredSamples() {return requiredSamples;}
    public void setRequiredSamples(int requiredSamples) {this.requiredSamples = requiredSamples;}

    public int getCurrentSampleCount() {return typingSamples.size();}
}
