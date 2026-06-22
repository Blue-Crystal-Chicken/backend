package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LocationRequest {
    @NotBlank(message = "Il nome è obbligatorio")
    private String name;

    @Valid
    private AddressRequest address;

    private String phoneCode;
    private String phoneNumber;
    
    @Min(value = 0)
    private Integer tables;
    
    private Boolean isOpen;
    private Boolean manuallyClosed;
    private String status;
}
