package com.konrad.checkinn.core.exception;

public class InvalidRequestException extends RuntimeException {
    public static final String INVALID_REQUEST_EXCEPTION = "Invalid Request";
    public static final String INVALID_ROLE_MESSAGE = "Requested role is not allowed for self-registration";
    public InvalidRequestException(String message) {
        super(message);
    }
}
