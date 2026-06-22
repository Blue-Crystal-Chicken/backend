package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuRequest {
    private String name;
    private Double price;
    private String description;
    private String imgPath;
    private MultipartFile image;
    private List<MenuProductRequest> products;
}
