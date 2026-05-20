package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity;

import java.time.LocalDateTime;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.key.UserMenuKey;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.PrePersist;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserFavoriteMenu  {

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

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

}
