package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents a location in the Blue Crystal Kitchen system.
 */
@Entity
@Table(name = "LOCATIONS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "locationIngredients")
public class LocationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "Name")
    @NotBlank(message = "Il nome è obbligatorio")
    private String name;

    @Column(name = "Address")
    @NotBlank(message = "L'indirizzo è obbligatorio")
    private String address;

    @Column(name = "City")
    @NotBlank(message = "La città è obbligatoria")
    private String city;

    @Column(name = "Phone_code")
    private String phoneCode;

    @Column(name = "Phone_number")
    private String phoneNumber;

    @Column(name = "Is_open")
    private Boolean isOpen;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean manuallyClosed;

    @Column(name = "Status")
    private String status;

    // Token-stazione: chiave per-sede usata dalla Cucina per autorizzare i cambi
    // di stato degli ordini della propria sede (vedi OrderController.updateStatus).
    // WRITE_ONLY: non viene esposto nelle GET pubbliche; lo legge solo l'Admin
    // dall'endpoint dedicato GET /api/locations/{id}/station-token.
    @Column(name = "Station_token")
    @com.fasterxml.jackson.annotation.JsonProperty(access = com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
    private String stationToken;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Bidirectional relationship with LocationIngredient
    @OneToMany(mappedBy = "location", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<LocationIngredient> locationIngredients;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
