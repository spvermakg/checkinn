package com.konrad.checkinn.listing.dto;

import com.konrad.checkinn.listing.entity.ListingType;
import jakarta.validation.constraints.AssertTrue;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;

@Data
public class SearchListingRequestDTO {
    private String state;
    private String country;
    private ListingType listingType;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Integer minGuests;
    private Set<Long> amenityIds;

    @AssertTrue(message = "minPrice must not exceed maxPrice")
    private boolean isPriceRangeValid() {
        if (minPrice == null || maxPrice == null) {
            return true;
        }
        return minPrice.compareTo(maxPrice) <= 0;
    }
}