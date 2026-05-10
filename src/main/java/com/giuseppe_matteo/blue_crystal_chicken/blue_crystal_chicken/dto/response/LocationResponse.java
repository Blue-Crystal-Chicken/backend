package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.response;

import lombok.Data;

@Data
public class LocationResponse {
    private Long id;
    private String name;
    private String address;
    private String city;
    private String phoneCode;
    private String phoneNumber;
    private Integer tables;
    private Boolean isOpen;
    private Boolean manuallyClosed;
    private String status;
}
