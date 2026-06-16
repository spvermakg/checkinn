package com.konrad.checkinn.listing.dto;

import com.konrad.checkinn.listing.entity.ListingType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;

@Data
public class UpdateListingRequestDTO {
    private String title;

    @Valid
    private AddressDTO address;

    private String description;

    private ListingType listingType;
    @Positive
    private BigDecimal price;
    private Set<Long> amenityIds;

    @Positive
    private Integer maxGuest;
}
