package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressRequest {

    @NotBlank(message = "Il tipo di indirizzo è obbligatorio")
    private String type;

    @NotBlank(message = "La via è obbligatoria")
    private String street;

    @NotBlank(message = "La città è obbligatoria")
    private String city;

    private String state;

    @NotBlank(message = "Il codice postale è obbligatorio")
    private String zipCode;

    @NotBlank(message = "Il paese è obbligatorio")
    private String country;
}
