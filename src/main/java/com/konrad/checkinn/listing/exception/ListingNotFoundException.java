package com.konrad.checkinn.listing.exception;

public class ListingNotFoundException extends RuntimeException {
  public static final String LISTING_NOT_FOUND = "No listing found";

  public ListingNotFoundException(String message) {
        super(message);
    }
}
