package com.konrad.checkinn.listing.repository;

import com.konrad.checkinn.core.entity.User;
import com.konrad.checkinn.listing.entity.Listing;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ListingRepository extends JpaRepository<Listing, Long>, JpaSpecificationExecutor<Listing> {
    @EntityGraph
    Optional<Listing> findByIdAndHostAndIsDeletedFalse(Long id, User host);

    @EntityGraph(attributePaths = {"amenities", "listingPhotos"})
    Page<Listing> findAllByIsDeletedFalse(Pageable pageable);

    @EntityGraph
    Optional<Listing> findByIdAndIsDeletedFalse(Long id);

    @Override
    @EntityGraph(attributePaths = {"amenities", "listingPhotos"})
    @NonNull
    Page<Listing> findAll(@NonNull Specification<Listing> spec, @NonNull Pageable pageable);

}
