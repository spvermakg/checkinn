package com.konrad.checkinn.listingPhoto.mapper;

import com.konrad.checkinn.listingPhoto.dto.PhotoResponseDTO;
import com.konrad.checkinn.listingPhoto.entity.ListingPhoto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface PhotoMapper {

    @Mappings({
            @Mapping(target = "imageUrl", ignore = true),
            @Mapping(target = "listingId", source = "listing.id"),
    })
    PhotoResponseDTO listingPhotoToPhotoResponseDTO(ListingPhoto listingPhoto);

}
