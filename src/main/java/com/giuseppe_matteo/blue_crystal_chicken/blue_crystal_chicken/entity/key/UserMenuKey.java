package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.key;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;


@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserMenuKey implements Serializable{

    @Column(name = "User_id")
    private Long userId;

    @Column(name = "Menu_id")
    private Long menuId;
    
}
