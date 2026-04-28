package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.repository;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.LocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<LocationEntity, Long> {

    Optional<LocationEntity> findByName(String name);

    Optional<LocationEntity> findByNameIgnoreCase(String name);

    Optional<LocationEntity> findByNameAndAddressAndCity(String name, String address, String city);

    boolean existsByName(String name);

    List<LocationEntity> findByCity(String city);

    List<LocationEntity> findByStatus(String status);

    List<LocationEntity> findByCityAndStatus(String city, String status);

    List<LocationEntity> findByNameContainingIgnoreCase(String name);
    
    List<LocationEntity> findByIsOpen(Boolean isOpen);

    @Modifying
    @Query("UPDATE LocationEntity l SET l.isOpen = :isOpen WHERE l.manuallyClosed = false")
    void setAllIsOpen(boolean isOpen);
}
