package com.konrad.checkinn.listing.entity;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class Address {
    private String houseNumber;
    private String streetAddress;
    private String state;
    private String country;
    private String pincode;
    private Double latitude;
    private Double longitude;
}