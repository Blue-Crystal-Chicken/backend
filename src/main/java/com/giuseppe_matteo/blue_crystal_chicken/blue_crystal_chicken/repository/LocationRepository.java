package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.repository;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.LocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<LocationEntity, Long> {

    Optional<LocationEntity> findByName(String name);

    boolean existsByName(String name);

    List<LocationEntity> findByCity(String city);

    List<LocationEntity> findByStatus(String status);

    List<LocationEntity> findByCityAndStatus(String city, String status);

    List<LocationEntity> findByNameContainingIgnoreCase(String name);
}
