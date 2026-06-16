package com.konrad.checkinn.listing.service;

import com.konrad.checkinn.core.entity.User;
import com.konrad.checkinn.core.exception.UserNotFoundException;
import com.konrad.checkinn.core.repository.UserRepository;
import com.konrad.checkinn.core.service.StorageService;
import com.konrad.checkinn.listing.dto.*;
import com.konrad.checkinn.listing.entity.Amenity;
import com.konrad.checkinn.listing.entity.Listing;
import com.konrad.checkinn.listing.exception.ListingNotFoundException;
import com.konrad.checkinn.listing.mapper.ListingMapper;
import com.konrad.checkinn.listing.repository.AmenityRepository;
import com.konrad.checkinn.listing.repository.ListingRepository;
import com.konrad.checkinn.listing.specification.ListingSpecification;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class ListingService {

    private final ListingRepository listingRepository;
    private final StorageService storageService;
    private final UserRepository userRepository;
    private final AmenityRepository amenityRepository;
    private final ListingMapper listingMapper;

    public ListingResponseDTO createListing(CreateListingRequestDTO createListingRequestDTO, UserDetails currentUser) {
        User host = getUserByEmailId(currentUser);

        Listing createdListing = listingMapper.createListingRequestDtoToListing(createListingRequestDTO);
        createdListing.setHost(host);
        createdListing.setAmenities(resolveAmenities(createListingRequestDTO.getAmenityIds()));

        Listing savedListing = listingRepository.save(createdListing);

        return mapAndHydrateListingResponse(savedListing);
    }

    public ListingResponseDTO updateListing(UpdateListingRequestDTO updateListingRequestDTO, Long listingId, UserDetails currentUser) {
        User host = getUserByEmailId(currentUser);

        Optional<Listing> foundListing = listingRepository.findByIdAndHostAndIsDeletedFalse(listingId, host);
        Listing existingListing = foundListing.orElseThrow(() -> new ListingNotFoundException(ListingNotFoundException.LISTING_NOT_FOUND));

        listingMapper.updateListingFromDTO(updateListingRequestDTO,existingListing);

        if (updateListingRequestDTO.getAmenityIds() != null) {
            existingListing.setAmenities(resolveAmenities(updateListingRequestDTO.getAmenityIds()));
        }

        Listing updatedListing = listingRepository.save(existingListing);

        return mapAndHydrateListingResponse(updatedListing);
    }

    public Page<ListingResponseDTO> searchListings(SearchListingRequestDTO searchRequest, Pageable pageable) {
        Specification<Listing> spec = Specification.where(ListingSpecification.isNotDeleted());

        if (searchRequest.getState() != null && !searchRequest.getState().isBlank()) {
            spec = spec.and(ListingSpecification.hasState(searchRequest.getState()));
        }
        if (searchRequest.getCountry() != null && !searchRequest.getCountry().isBlank()) {
            spec = spec.and(ListingSpecification.hasCountry(searchRequest.getCountry()));
        }
        if (searchRequest.getListingType() != null) {
            spec = spec.and(ListingSpecification.hasListingType(searchRequest.getListingType()));
        }
        if (searchRequest.getMinPrice() != null) {
            spec = spec.and(ListingSpecification.hasPriceGreaterThanOrEqual(searchRequest.getMinPrice()));
        }
        if (searchRequest.getMaxPrice() != null) {
            spec = spec.and(ListingSpecification.hasPriceLessThanOrEqual(searchRequest.getMaxPrice()));
        }
        if (searchRequest.getMinGuests() != null) {
            spec = spec.and(ListingSpecification.hasMinGuests(searchRequest.getMinGuests()));
        }
        if (searchRequest.getAmenityIds() != null && !searchRequest.getAmenityIds().isEmpty()) {
            spec = spec.and(ListingSpecification.hasAmenities(searchRequest.getAmenityIds()));
        }

        Page<Listing> listings = listingRepository.findAll(spec, pageable);
        return listings.map(this::mapAndHydrateListingResponse);
    }

    public List<AmenityResponseDTO> getAllAmenities() {
        return amenityRepository.findAll()
                .stream()
                .map(listingMapper::amenityToAmenityResponseDTO)
                .toList();
    }

    private Set<Amenity> resolveAmenities(Set<Long> amenityIds) {
        if (amenityIds == null || amenityIds.isEmpty()) {
            return new HashSet<>();
        }
        Set<Amenity> amenities = new HashSet<>(amenityRepository.findAllById(amenityIds));
        if (amenities.size() != amenityIds.size()) {
            throw new IllegalArgumentException("One or more amenity IDs are invalid");
        }
        return amenities;
    }

    private @NonNull User getUserByEmailId(UserDetails currentUser) {
        String userEmail = currentUser.getUsername();
        Optional<User> foundHost = userRepository.findUserByEmail(userEmail);
        return foundHost.orElseThrow(() -> new UserNotFoundException(UserNotFoundException.NO_USER_FOUND));
    }

    public void softDeleteListingById(Long listingId, UserDetails currentUser) {
        User host = getUserByEmailId(currentUser);

        Optional<Listing> listing = listingRepository.findByIdAndHostAndIsDeletedFalse(listingId, host);
        Listing existingListing = listing.orElseThrow(() -> new ListingNotFoundException(ListingNotFoundException.LISTING_NOT_FOUND));

        existingListing.setDeleted(true);

        listingRepository.save(existingListing);
    }

    public ListingResponseDTO findListingById(Long listingId) {
        Optional<Listing> listing = listingRepository.findByIdAndIsDeletedFalse(listingId);

        Listing foundListing = listing.orElseThrow(() -> new ListingNotFoundException(ListingNotFoundException.LISTING_NOT_FOUND));

        return mapAndHydrateListingResponse(foundListing);
    }

    public Page<ListingResponseDTO> findAllListings(Pageable pageable) {
        Page<Listing> listingPage = listingRepository.findAllByIsDeletedFalse(pageable);

        return listingPage.map(this::mapAndHydrateListingResponse);
    }

    private @NonNull ListingResponseDTO mapAndHydrateListingResponse(Listing listing) {
        ListingResponseDTO response = listingMapper.listingToListingResponseDTO(listing);
        hydratePhotoUrls(response);
        return response;
    }

    private void hydratePhotoUrls(ListingResponseDTO dto) {
        if (dto.getListingPhotos() != null) {
            dto.getListingPhotos().forEach(photo ->
                    photo.setImageUrl(storageService.getFileUrl(photo.getImageKey()))
            );
        }
    }
}
