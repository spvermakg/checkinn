package com.konrad.checkinn.listing.controller;

import com.konrad.checkinn.listing.dto.*;
import com.konrad.checkinn.listing.service.ListingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/v1/listings")
public class ListingController {

    private final ListingService listingService;

    @PreAuthorize("hasRole('HOST')")
    @PostMapping
    public ResponseEntity<ListingResponseDTO> createListing(
            @Valid @RequestBody CreateListingRequestDTO createListingRequestDTO,
            @AuthenticationPrincipal UserDetails currentUser) {

        ListingResponseDTO listing = listingService.createListing(createListingRequestDTO, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(listing);
    }


    @PreAuthorize("hasRole('HOST')")
    @PatchMapping("/{id}")
    public ResponseEntity<ListingResponseDTO> updateListing(
            @Valid @RequestBody UpdateListingRequestDTO updateListingRequestDTO,
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        ListingResponseDTO listing = listingService.updateListing(
                updateListingRequestDTO,
                id,
                currentUser
        );

        return ResponseEntity.status(HttpStatus.OK).body(listing);
    }

    @PreAuthorize("hasRole('HOST')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteListing(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        listingService.softDeleteListingById(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ListingResponseDTO> fetchListing(
            @PathVariable Long id
    ){
        ListingResponseDTO listing = listingService.findListingById(id);
        return ResponseEntity.status(HttpStatus.OK).body(listing);
    }

    @GetMapping
    public ResponseEntity<Page<ListingResponseDTO>> fetchAllListings(Pageable pageable){
        Page<ListingResponseDTO> listing = listingService.findAllListings(pageable);
        return ResponseEntity.status(HttpStatus.OK).body(listing);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ListingResponseDTO>> searchListings(
            @Valid @ModelAttribute SearchListingRequestDTO searchRequest,
            Pageable pageable
    ) {
        Page<ListingResponseDTO> results = listingService.searchListings(searchRequest, pageable);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/amenities")
    public ResponseEntity<List<AmenityResponseDTO>> fetchAllAmenities() {
        List<AmenityResponseDTO> amenities = listingService.getAllAmenities();
        return ResponseEntity.ok(amenities);
    }
}
