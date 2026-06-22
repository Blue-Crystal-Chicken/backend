package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.mapper;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.response.MenuProductResponse;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.response.MenuResponse;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.MenuEntity;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.MenuProduct;
import java.util.List;

public interface MenuMapper {
    MenuResponse toResponse(MenuEntity menu);
    List<MenuResponse> toResponseList(List<MenuEntity> menus);
    MenuProductResponse toMenuProductResponse(MenuProduct menuProduct);
    List<MenuProductResponse> toMenuProductResponseList(List<MenuProduct> menuProducts);
}
