package com.konrad.checkinn.core.exception;

public class UserNotFoundException extends RuntimeException {
    public static final String NO_USER_FOUND = "No user found";

    public UserNotFoundException(String message) {
        super(message);
    }
}
