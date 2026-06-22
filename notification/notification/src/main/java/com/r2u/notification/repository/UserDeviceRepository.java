package com.r2u.notification.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.r2u.notification.entity.model.UserDevice;

@Repository
public interface UserDeviceRepository extends JpaRepository<UserDevice, Long> {

    List<UserDevice> findAllByUserId(String userId);

    Optional<UserDevice> findByUserIdAndDevice_Id(String userId, Long deviceId);

    List<UserDevice> findAllByDevice_Id(Long deviceId);

    /**
     * Disattiva tutte le associazioni di un utente tranne quella con il deviceId specificato.
     */
    @Modifying
    @Query("UPDATE UserDevice ud SET ud.isActive = false WHERE ud.userId = :userId AND ud.device.id <> :deviceId")
    void deactivateOtherDevicesForUser(@Param("userId") String userId, @Param("deviceId") Long deviceId);

    List<UserDevice> findByIsActiveTrue();

    @Modifying
    @Query("UPDATE UserDevice ud SET ud.isActive = false WHERE ud.lastSeenAt < :threshold AND ud.isActive = true")
    void deactivateOlderThan(@Param("threshold") LocalDateTime threshold);
}
