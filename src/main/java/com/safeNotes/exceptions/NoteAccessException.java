package com.safeNotes.exceptions;

public class NoteAccessException extends Exception {
    public NoteAccessException(String message) {super(message);}

    public NoteAccessException(String message, Throwable cause) {super(message, cause);}
}
