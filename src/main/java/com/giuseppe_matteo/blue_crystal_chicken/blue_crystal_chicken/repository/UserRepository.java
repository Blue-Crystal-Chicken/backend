package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity,Long>{

    Optional<UserEntity> findByEmail(String email);
    
    boolean existsByEmail(String email); 

    
}
