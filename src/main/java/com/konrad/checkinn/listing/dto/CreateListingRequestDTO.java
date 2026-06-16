package com.konrad.checkinn.listing.dto;

import com.konrad.checkinn.listing.entity.ListingType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;

@Data
public class CreateListingRequestDTO {

    @NotBlank(message = "Please provide a title for listing")
    private String title;

    @NotNull
    @Valid
    private AddressDTO address;

    @NotBlank(message = "Please provide a description for the listing")
    private String description;

    @NotNull
    private ListingType listingType;

    @NotNull
    @DecimalMin(value = "100.00")
    @Positive
    private BigDecimal price;

    @NotNull
    private Set<Long> amenityIds;

    @NotNull
    @Min(value = 1)
    private Integer maxGuest;
}
