package com.konrad.checkinn.listing.entity;

import com.konrad.checkinn.core.entity.User;
import com.konrad.checkinn.listingPhoto.entity.ListingPhoto;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "listings")
public class Listing {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Embedded
    private Address address;

    private String description;

    @Enumerated(value = EnumType.STRING)
    private ListingType listingType;

    private BigDecimal price;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "listing_amenity",
            joinColumns = @JoinColumn(name = "listing_id"),
            inverseJoinColumns = @JoinColumn(name = "amenity_id")
    )
    private Set<Amenity> amenities = new HashSet<>();

    private Integer maxGuest;

    @ManyToOne
    private User host;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    private boolean isDeleted = false;

    @OneToMany(mappedBy = "listing", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ListingPhoto> listingPhotos;

}
