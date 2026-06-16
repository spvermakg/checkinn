package com.konrad.checkinn.listingPhoto.repository;

import com.konrad.checkinn.listingPhoto.entity.ListingPhoto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ListingPhotoRepository extends JpaRepository<ListingPhoto, Long> {

    List<ListingPhoto> getAllListingPhotosByListingId(Long listingId);
}
