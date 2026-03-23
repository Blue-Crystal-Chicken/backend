package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String email;
    private String name;
    private String surname;
    private String phone;
    private String gender;
    private String birthday;
    private List<String> roles;

    public JwtResponse(String token, Long id, String email, String name, String surname, String phone, String gender, String birthday, List<String> roles) {
        this.token = token;
        this.id = id;
        this.email = email;
        this.name = name;
        this.surname = surname;
        this.phone = phone;
        this.gender = gender;
        this.birthday = birthday;
        this.roles = roles;
    }

}