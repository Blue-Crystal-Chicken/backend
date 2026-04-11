package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Data
public class OfferRequest {
    private String name;
    private String description;
    private Double price;
    private String imgPath;
    private MultipartFile image;
    private List<Long> menuIds;
}
