package com.konrad.checkinn.core.exception;

public class StorageOperationException extends RuntimeException {
    public static final String FILE_UPLOAD_FAILURE = "Could not upload the file";
    public static final String FILE_URL_FETCH_FAILURE = "Could not able to fetch the fileUrl";
    public static final String FILE_DELETION_FAILURE = "Could not able to delete the file";
    public StorageOperationException(String message) {
        super(message);
    }
}
