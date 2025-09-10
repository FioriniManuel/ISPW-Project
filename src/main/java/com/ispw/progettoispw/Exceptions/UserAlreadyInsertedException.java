package com.ispw.progettoispw.Exceptions;

public class UserAlreadyInsertedException extends RuntimeException {
    public UserAlreadyInsertedException(String message) {
        super(message);
    }
}
