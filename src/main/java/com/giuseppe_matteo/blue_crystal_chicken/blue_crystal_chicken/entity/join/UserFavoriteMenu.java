package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.join;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.AuditingField;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.join.key.UserMenuKey;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.user.UserEntity;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.menu.MenuEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import lombok.*;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class UserFavoriteMenu extends AuditingField {

    @EmbeddedId
    private UserMenuKey id;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToOne
    @MapsId("menuId")
    @JoinColumn(name = "menu_id")
    private MenuEntity menu;
}
