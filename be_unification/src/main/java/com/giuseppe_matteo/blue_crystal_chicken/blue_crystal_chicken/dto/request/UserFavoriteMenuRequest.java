package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request;

import lombok.Data;
@Data
public class UserFavoriteMenuRequest {
    private Long userId;
    private Long menuId;
}
