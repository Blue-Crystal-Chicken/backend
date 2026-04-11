package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.mapper.Impl;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.mapper.MenuMapper;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.mapper.OfferMapper;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.response.OfferProductResponse;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.response.OfferResponse;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.OfferEntity;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.OfferProduct;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OfferMapperImpl implements OfferMapper {

    private final ModelMapper modelMapper;
    private final MenuMapper menuMapper;

    public OfferMapperImpl(ModelMapper modelMapper, MenuMapper menuMapper) {
        this.modelMapper = modelMapper;
        this.menuMapper = menuMapper;
    }

    @Override
    public OfferResponse toResponse(OfferEntity offer) {
        if (offer == null) return null;

        OfferResponse response = modelMapper.map(offer, OfferResponse.class);
        response.setImgPath(offer.getImgPath());

        if (offer.getMenus() != null) {
            response.setMenus(menuMapper.toResponseList(offer.getMenus()));
        } else {
            response.setMenus(Collections.emptyList());
        }

        if (offer.getOfferProducts() != null) {
            response.setOfferProducts(toOfferProductResponseList(offer.getOfferProducts()));
        } else {
            response.setOfferProducts(Collections.emptyList());
        }

        return response;
    }

    @Override
    public List<OfferResponse> toResponseList(List<OfferEntity> offers) {
        if (offers == null) return Collections.emptyList();
        return offers.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public OfferProductResponse toOfferProductResponse(OfferProduct offerProduct) {
        if (offerProduct == null) return null;

        OfferProductResponse response = new OfferProductResponse();
        response.setProductId(offerProduct.getProduct().getId());
        response.setProductName(offerProduct.getProduct().getName());
        response.setQuantity(offerProduct.getQuantity());
        response.setUnitPrice(offerProduct.getOffer().getPrice());

        return response;
    }

    @Override
    public List<OfferProductResponse> toOfferProductResponseList(List<OfferProduct> offerProducts) {
        if (offerProducts == null) return Collections.emptyList();
        return offerProducts.stream()
                .map(this::toOfferProductResponse)
                .collect(Collectors.toList());
    }
}
