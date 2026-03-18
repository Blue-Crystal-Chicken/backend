package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.UserEntity;

// Anche lasciando questa interfaccia VUOTA, Spring Data + Hibernate genera a runtime
// l'implementazione di tutti i metodi ereditati da JpaRepository:
// save(), findById(), findAll(), deleteById(), count(), existsById(), findAll(Pageable), ...
// Tutto il CRUD + paginazione + sorting gratis, zero boilerplate.
public interface UserRepository extends JpaRepository<UserEntity,Long>{

    Optional<UserEntity> findByEmail(String email);
    
    boolean existsByEmail(String email); 

    


    
}
