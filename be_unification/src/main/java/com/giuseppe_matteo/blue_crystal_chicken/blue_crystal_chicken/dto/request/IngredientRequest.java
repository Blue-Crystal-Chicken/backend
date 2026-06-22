package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IngredientRequest {

    @NotBlank(message = "Il nome è obbligatorio")
    @Size(min = 3, max = 50, message = "Il nome deve essere compreso tra 3 e 50 caratteri")
    private String name;

    @Size(max = 255, message = "La descrizione non può superare i 255 caratteri")
    private String description;

    private Double price;

    private Double quantity;
}
