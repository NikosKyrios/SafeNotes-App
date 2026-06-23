package com.safeNotes.exceptions;

public class HashingException extends Exception {
    public HashingException(String text) {super(text);}
    public HashingException(String text, Throwable cause) {super(text, cause);}
}
