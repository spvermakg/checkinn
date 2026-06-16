package com.konrad.checkinn.listingPhoto.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PhotoUploadRequestDTO {
    private boolean isPrimary;
    @NotBlank(message = "Photo title is required")
    @Size(max = 255, message = "Photo title must not exceed 255 characters")
    private String photoTitle;
}
