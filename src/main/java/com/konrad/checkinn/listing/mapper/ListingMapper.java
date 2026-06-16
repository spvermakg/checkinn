package com.konrad.checkinn.listing.mapper;


import com.konrad.checkinn.core.mapper.CoreUserMapper;
import com.konrad.checkinn.listing.dto.AmenityResponseDTO;
import com.konrad.checkinn.listing.dto.CreateListingRequestDTO;
import com.konrad.checkinn.listing.dto.ListingResponseDTO;
import com.konrad.checkinn.listing.dto.UpdateListingRequestDTO;
import com.konrad.checkinn.listing.entity.Amenity;
import com.konrad.checkinn.listing.entity.Listing;
import com.konrad.checkinn.listingPhoto.dto.ListingPhotoDTO;
import com.konrad.checkinn.listingPhoto.entity.ListingPhoto;
import com.konrad.checkinn.listingPhoto.mapper.PhotoMapper;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {CoreUserMapper.class, PhotoMapper.class})
public interface ListingMapper {

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "updatedAt", ignore = true),
            @Mapping(target = "host", ignore = true),
            @Mapping(target = "listingPhotos", ignore = true),
            @Mapping(target = "amenities", ignore = true),
    })
    Listing createListingRequestDtoToListing(CreateListingRequestDTO createListingRequestDTO);


    ListingResponseDTO listingToListingResponseDTO (Listing listing);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "amenities", ignore = true)
    void updateListingFromDTO(UpdateListingRequestDTO dto, @MappingTarget Listing listing);


    AmenityResponseDTO amenityToAmenityResponseDTO(Amenity amenity);

    @Mapping(target = "imageUrl", ignore = true)
    @Mapping(target = "listingId", source = "listing.id")
    ListingPhotoDTO listingPhotoToListingPhotoDTO(ListingPhoto listingPhoto);
}
