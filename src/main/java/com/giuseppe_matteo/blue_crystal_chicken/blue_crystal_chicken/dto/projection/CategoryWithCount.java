package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.projection;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.Category;

public interface CategoryWithCount {
    Category getCategory();
    Long getCount();
}
