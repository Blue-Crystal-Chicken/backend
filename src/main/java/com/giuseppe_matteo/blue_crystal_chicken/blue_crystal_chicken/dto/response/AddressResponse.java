package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.response;

import lombok.Data;

@Data
public class AddressResponse {
    private Long id;
    private String type;
    private String street;
    private String city;
    private String state;
    private String zipCode;
    private String country;
}
