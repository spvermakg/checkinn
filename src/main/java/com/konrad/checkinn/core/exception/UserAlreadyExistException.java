package com.konrad.checkinn.core.exception;

public class UserAlreadyExistException extends RuntimeException {
    public static final String EXISTING_EMAIL_MESSAGE = "User with this Email Already exist";
    public static final String EXISTING_MOBILE_MESSAGE = "User with this Mobile Already exist";

    public UserAlreadyExistException(String message){
        super(message);
    }
}
