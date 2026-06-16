package com.konrad.checkinn.listingPhoto.exception;

public class IllegalFileFormatException extends RuntimeException {
    public static final String IMAGE_FILE_REQUIRED = "Image file required!";

    public IllegalFileFormatException(String message) {
        super(message);
    }
}
