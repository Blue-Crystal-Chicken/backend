package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Represents a user in the Blue Crystal Kitchen system.
 */
@Entity
@Table(name = "USERS")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "Name")
    private String name;

    @Column(name = "Surname")
    private String surname;

    @Column(name = "Birthday")
    private LocalDate birthday;

    @Column(name = "Email")
    private String email;

    @Column(name = "Password")
    private String password;

    @Column(name = "Gender")
    private String gender;

    @Column(name = "Phone")
    private String phone;

    // Bidirectional relationship with Order
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Order> orders;
}