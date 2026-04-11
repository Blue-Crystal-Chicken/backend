package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.mapper;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.response.OfferProductResponse;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.response.OfferResponse;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.OfferEntity;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.OfferProduct;
import java.util.List;

public interface OfferMapper {
    OfferResponse toResponse(OfferEntity offer);
    List<OfferResponse> toResponseList(List<OfferEntity> offers);
    OfferProductResponse toOfferProductResponse(OfferProduct offerProduct);
    List<OfferProductResponse> toOfferProductResponseList(List<OfferProduct> offerProducts);
}
