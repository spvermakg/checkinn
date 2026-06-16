package com.konrad.checkinn.listingPhoto.dto;

import lombok.Data;

@Data
public class ListingPhotoDTO {
    private Long id;
    private String imageUrl;
    private String imageKey;
    private String photoTitle;
    private Integer displayOrder;
    private boolean isPrimary = false;
    private Long listingId;
}
