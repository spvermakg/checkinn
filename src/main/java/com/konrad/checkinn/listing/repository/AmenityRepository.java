package com.konrad.checkinn.listing.repository;

import com.konrad.checkinn.listing.entity.Amenity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AmenityRepository extends JpaRepository<Amenity, Long> {
}