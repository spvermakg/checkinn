package com.konrad.checkinn.listingPhoto.exception;

public class ListingPhotoNotFoundException extends RuntimeException {
    public static final String PHOTO_NOT_FOUND = "Photo Not Found!";
    public ListingPhotoNotFoundException(String message) {
        super(message);
    }
}
