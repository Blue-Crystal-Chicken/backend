package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.mapper.Impl;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.mapper.MenuMapper;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.response.MenuProductResponse;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.response.MenuResponse;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.MenuEntity;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.MenuProduct;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MenuMapperImpl implements MenuMapper {

    private final ModelMapper modelMapper;

    public MenuMapperImpl(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public MenuResponse toResponse(MenuEntity menu) {
        if (menu == null) return null;

        MenuResponse response = modelMapper.map(menu, MenuResponse.class);

        if (menu.getMenuProducts() != null) {
            response.setMenuProducts(menu.getMenuProducts().stream()
                    .map(this::toMenuProductResponse)
                    .collect(Collectors.toList()));
        } else {
            response.setMenuProducts(Collections.emptyList());
        }

        return response;
    }

    @Override
    public List<MenuResponse> toResponseList(List<MenuEntity> menus) {
        if (menus == null) return Collections.emptyList();
        return menus.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<MenuProductResponse> toMenuProductResponseList(List<MenuProduct> menuProducts) {
        if (menuProducts == null) return Collections.emptyList();
        return menuProducts.stream()
                .map(this::toMenuProductResponse)
                .collect(Collectors.toList());
    }

    @Override
    public MenuProductResponse toMenuProductResponse(MenuProduct menuProduct) {
        if (menuProduct == null) return null;

        MenuProductResponse response = new MenuProductResponse();
        response.setProductId(menuProduct.getProduct().getId());
        response.setProductName(menuProduct.getProduct().getName());
        response.setQuantity(menuProduct.getQuantity());
        response.setObligatory(menuProduct.getObligatory());
        response.setUnitPrice(menuProduct.getProduct().getPrice());

        return response;
    }
}
