package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.location;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.AuditingField;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.join.LocationIngredient;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.address.Address;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.util.List;

/**
 * Represents a location in the Blue Crystal Chicken system.
 */
@Entity
@Table(name = "LOCATIONS")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "locationIngredients")
public class LocationEntity extends AuditingField {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "Name")
    @NotBlank(message = "Il nome è obbligatorio")
    private String name;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "address_id")
    private Address address;

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

    @Column(name = "Tables", columnDefinition = "integer default 0")
    @Min(value = 0)
    private Integer tables;

    // Token-stazione: chiave per-sede usata da Cucina/Tabellone/Cassa per autorizzare
    // i cambi di stato degli ordini della propria sede. Write-only: non esce nelle GET.
    @Column(name = "Station_token")
    @com.fasterxml.jackson.annotation.JsonProperty(access = com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
    private String stationToken;

    // Bidirectional relationship with LocationIngredient.
    // @JsonIgnore: come per IngredientEntity, evita il ciclo
    // Location -> locationIngredients -> LocationIngredient.location -> ...
    @OneToMany(mappedBy = "location", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<LocationIngredient> locationIngredients;
}
