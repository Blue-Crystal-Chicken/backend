package com.r2u.notification.scheduler;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.r2u.notification.repository.UserDeviceRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeviceCleanupScheduler {

    private final UserDeviceRepository userDeviceRepository;

    @Scheduled(cron = "0 0 3 * * *") // Esegui ogni giorno alle 3 del mattino
    @Transactional
    public void cleanUpDeviceNotifications() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(30);
        log.info("Running device cleanup, threshold: {}", threshold);
        userDeviceRepository.deactivateOlderThan(threshold);
        log.info("Device notifications cleaned up.");
    }
}
