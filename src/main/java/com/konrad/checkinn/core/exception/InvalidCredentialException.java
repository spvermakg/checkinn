package com.konrad.checkinn.core.exception;

public class InvalidCredentialException extends RuntimeException {
    public static final String WRONG_CREDENTIALS = "Credentials are incorrect";

    public InvalidCredentialException(String message) {
        super(message);
    }
}
