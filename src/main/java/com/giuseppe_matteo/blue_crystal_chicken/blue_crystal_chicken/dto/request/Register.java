package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.Role;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class Register {
    @NotBlank
    private String name;
    @NotBlank
    private String surname;
    @NotBlank
    @Email
    private String email;
    @NotBlank
    private String phone;
    @NotBlank
    private String gender;
    @NotBlank
    private String birthday;
    @NotBlank
    private String password;

    private Role role;

    public Register(String name, String surname, String email, String phone, String gender, String birthday, String password) {
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.phone = phone;
        this.gender = gender;
        this.birthday = birthday;
        this.password = password;
        this.role = Role.USER;
    }
}