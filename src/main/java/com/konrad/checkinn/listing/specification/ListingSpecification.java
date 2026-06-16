package com.konrad.checkinn.listing.specification;

import com.konrad.checkinn.listing.entity.Listing;
import com.konrad.checkinn.listing.entity.ListingType;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.Set;


public class ListingSpecification {

    private ListingSpecification() {
    }

    public static Specification<Listing> isNotDeleted() {
        return (root, query, cb) -> cb.isFalse(root.get("isDeleted"));
    }

    public static Specification<Listing> hasState(String state) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("address").get("state")), "%" + state.toLowerCase() + "%");
    }

    public static Specification<Listing> hasCountry(String country) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("address").get("country")), "%" + country.toLowerCase() + "%");
    }

    public static Specification<Listing> hasListingType(ListingType listingType) {
        return (root, query, cb) ->
                cb.equal(root.get("listingType"), listingType);
    }

    public static Specification<Listing> hasPriceGreaterThanOrEqual(BigDecimal minPrice) {
        return (root, query, cb) ->
                cb.greaterThanOrEqualTo(root.get("price"), minPrice);
    }

    public static Specification<Listing> hasPriceLessThanOrEqual(BigDecimal maxPrice) {
        return (root, query, cb) ->
                cb.lessThanOrEqualTo(root.get("price"), maxPrice);
    }

    public static Specification<Listing> hasMinGuests(Integer minGuests) {
        return (root, query, cb) ->
                cb.greaterThanOrEqualTo(root.get("maxGuest"), minGuests);
    }

    public static Specification<Listing> hasAmenities(Set<Long> amenityIds) {
        return (root, query, cb) -> {
            var subquery = query.subquery(Long.class);
            var subRoot = subquery.from(Listing.class);
            var amenitiesJoin = subRoot.join("amenities");
            subquery.select(subRoot.get("id"))
                    .where(amenitiesJoin.get("id").in(amenityIds))
                    .groupBy(subRoot.get("id"))
                    .having(cb.equal(cb.countDistinct(amenitiesJoin.get("id")), (long) amenityIds.size()));
            return root.get("id").in(subquery);
        };
    }
}