package com.konrad.checkinn.listing.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "amenities")
public class Amenity {
    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "amenity_seq")
    @SequenceGenerator(name = "amenity_seq", sequenceName = "amenity_seq", allocationSize = 1)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;
}
