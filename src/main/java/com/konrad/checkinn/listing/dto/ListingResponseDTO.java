package com.konrad.checkinn.listing.dto;

import com.konrad.checkinn.core.dto.HostSummaryDTO;
import com.konrad.checkinn.listing.entity.ListingType;
import com.konrad.checkinn.listingPhoto.dto.ListingPhotoDTO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Data
public class ListingResponseDTO {
    private Long id;
    private String title;
    private AddressDTO address;
    private String description;
    private ListingType listingType;
    private BigDecimal price;
    private Set<AmenityResponseDTO> amenities;
    private Integer maxGuest;
    private HostSummaryDTO host;
    private List<ListingPhotoDTO> listingPhotos;
}
