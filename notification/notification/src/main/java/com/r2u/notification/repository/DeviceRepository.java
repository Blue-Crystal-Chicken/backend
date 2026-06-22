package com.r2u.notification.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.r2u.notification.entity.model.DeviceEntity;

@Repository
public interface DeviceRepository extends JpaRepository<DeviceEntity, Long>{

    Optional<DeviceEntity> findByDeviceToken(String deviceToken);
    
    Boolean existsByDeviceToken(String deviceToken);
    
}
