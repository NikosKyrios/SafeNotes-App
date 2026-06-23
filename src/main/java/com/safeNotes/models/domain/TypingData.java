package com.safeNotes.models.domain;

public class TypingData {
    private long totalTypingTime;
    private boolean isCopyPaste;

    public TypingData() {}

    public TypingData(long totalTypingTime, boolean isCopyPaste) {
        this.totalTypingTime = totalTypingTime;
        this.isCopyPaste = isCopyPaste;
    }

    public long getTotalTypingTime() {return totalTypingTime;}
    public void setTotalTypingTime(long totalTypingTime) {this.totalTypingTime = totalTypingTime;}

    public boolean isCopyPaste() {return isCopyPaste;}
    public void setIsCopyPaste(boolean isCopyPaste) {this.isCopyPaste = isCopyPaste;}
}
