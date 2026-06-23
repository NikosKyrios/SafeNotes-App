package com.safeNotes.exceptions;

public class EncryptionException extends Exception {
    public EncryptionException(String text) {super(text);}
    public EncryptionException(String text, Throwable cause) {super(text, cause);}
}
