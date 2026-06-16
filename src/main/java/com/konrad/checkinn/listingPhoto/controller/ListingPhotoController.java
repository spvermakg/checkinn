package com.konrad.checkinn.listingPhoto.controller;

import com.konrad.checkinn.listingPhoto.dto.PhotoResponseDTO;
import com.konrad.checkinn.listingPhoto.dto.PhotoUploadRequestDTO;
import com.konrad.checkinn.listingPhoto.service.ListingPhotoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/v1/listings/{listingId}/photos/")
public class ListingPhotoController {
    private final ListingPhotoService listingPhotoService;

    @PreAuthorize("hasRole('HOST')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PhotoResponseDTO> uploadPhoto(
            @RequestPart("file") MultipartFile file,
            @PathVariable Long listingId,
            @Valid @RequestPart PhotoUploadRequestDTO photoUploadRequestDTO,
            @AuthenticationPrincipal UserDetails currentUser) {

        PhotoResponseDTO photoResponseDTO = listingPhotoService.uploadPhoto(file, photoUploadRequestDTO, listingId, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(photoResponseDTO);
    }


    @PreAuthorize("hasRole('HOST')")
    @DeleteMapping("/{photoId}")
    public ResponseEntity<Object> deletePhoto(
            @PathVariable Long photoId,
            @PathVariable Long listingId,
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        listingPhotoService.deletePhoto(photoId, listingId, currentUser);
        return ResponseEntity.noContent().build();
    }


    @PreAuthorize("hasRole('HOST')")
    @PatchMapping("/{photoId}")
    public ResponseEntity<Object> setPhotoPrimary(@PathVariable Long photoId, @PathVariable Long listingId, @AuthenticationPrincipal UserDetails userDetails) {
        listingPhotoService.setPhotoPrimary(photoId, listingId, userDetails);
        return ResponseEntity.noContent().build();
    }
}
