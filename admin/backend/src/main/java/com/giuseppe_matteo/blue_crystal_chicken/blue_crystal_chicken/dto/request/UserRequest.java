package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRequest {
    private String name;
    private String surname;
    private String email;
    private String phone;
    private String gender;
    private String birthday;
    private String password;
    private Role role;

}
