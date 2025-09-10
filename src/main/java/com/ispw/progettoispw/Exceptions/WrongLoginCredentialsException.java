package com.ispw.progettoispw.Exceptions;

public class WrongLoginCredentialsException extends RuntimeException {
    public WrongLoginCredentialsException(String message) {
        super(message);
    }
}
