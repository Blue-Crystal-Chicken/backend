package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.user;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.AuditingField;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.order.OrderEntity;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.location.LocationEntity;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.join.UserFavoriteProduct;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.address.Address;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.ColumnDefault;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * Represents a user in the Blue Crystal Chicken system.
 */
@Entity
@Table(name = "USERS", uniqueConstraints = {
        @UniqueConstraint(columnNames = "Email")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity extends AuditingField {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "Name")
    @NotBlank(message = "Il nome è obbligatorio")
    @Size(min = 3, max = 50, message = "Il nome deve essere compreso tra 3 e 20 caratteri")
    private String name;

    @Column(name = "Surname")
    @NotBlank(message = "Il cognome è obbligatrio")
    @Size(min = 3, max = 50, message = "Il cognome deve essere compreso tra 3 e 20 caratteri")
    private String surname;

    @Column(name = "Birthday")
    private LocalDate birthday;

    @Column(name = "Email")
    @NotBlank(message = "L'email è obbligatoria")
    @Email(message = "L'email non è valida")
    private String email;

    @Column(name = "Password")
    @NotBlank(message = "La password è obbligatoria")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Column(name = "Gender")
    private String gender;

    @Column(name = "Phone")
    @NotBlank(message = "Il numero di telefono è obbligatorio")
    @Size(min = 10, max = 10, message = "Il numero di telefono deve essere composto da 10 cifre")
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    @ColumnDefault("USER")
    private Role role;

    // Bidirectional relationship with Order
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<OrderEntity> orders;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Location_id")
    @JsonIgnoreProperties("locationIngredients")
    private LocationEntity location;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "address_id")
    private Address address;

    @OneToMany(mappedBy = "user")
    @JsonIgnore
    private Set<UserFavoriteProduct> favoriteProducts;
}
