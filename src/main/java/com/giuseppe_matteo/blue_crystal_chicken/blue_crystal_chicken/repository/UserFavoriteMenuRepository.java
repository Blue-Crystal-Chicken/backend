package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.repository;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.UserFavoriteMenu;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.key.UserMenuKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserFavoriteMenuRepository extends JpaRepository<UserFavoriteMenu, UserMenuKey> {
    List<UserFavoriteMenu> findByIdUserId(Long userId);
}
