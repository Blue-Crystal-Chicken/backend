package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents a user in the Blue Crystal Kitchen system.
 */
@Entity
@Table(name = "USERS",uniqueConstraints = {
    @UniqueConstraint(columnNames = "Email")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPOJO {

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
    @Size(min = 6, max = 20, message = "La password deve essere compresa tra 6 e 20 caratteri")
    private String password;

    @Column(name = "Gender")
    private String gender;

    @Column(name = "Phone")
    @NotBlank(message = "Il numero di telefono è obbligatorio")
    @Size(min = 10, max = 10, message = "Il numero di telefono deve essere composto da 10 cifre")
    private String phone;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Bidirectional relationship with Order
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<OrderPOJO> orders;

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