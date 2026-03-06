package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

/**
 * Represents a location in the Blue Crystal Kitchen system.
 */
@Entity
@Table(name = "LOCATIONS")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "Name")
    private String name;

    @Column(name = "Address")
    private String address;

    @Column(name = "City")
    private String city;

    @Column(name = "Phone_code")
    private String phoneCode;

    @Column(name = "Phone_number")
    private String phoneNumber;

    @Column(name = "Opening_hours")
    private String openingHours;

    @Column(name = "Status")
    private String status;

    // Bidirectional relationship with LocationIngredient
    @OneToMany(mappedBy = "location", cascade = CascadeType.ALL)
    private List<LocationIngredient> locationIngredients;
}