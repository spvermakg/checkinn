package com.konrad.checkinn.listingPhoto.dto;

import lombok.Data;

@Data
public class PhotoResponseDTO {
    private Long id;
    private String imageUrl;
    private String photoTitle;
    private boolean isPrimary;
    private Long listingId;
    private Integer displayOrder;
}
