package com.r2u.notification.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.r2u.notification.dto.mapper.UserDeviceMapper;
import com.r2u.notification.dto.request.UserDeviceRequest;
import com.r2u.notification.dto.response.UserDeviceResponse;
import com.r2u.notification.entity.model.DeviceEntity;
import com.r2u.notification.entity.model.UserDevice;
import com.r2u.notification.exception.UserDeviceNotFoundException;
import com.r2u.notification.repository.DeviceRepository;
import com.r2u.notification.repository.UserDeviceRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDeviceService {

    private final UserDeviceRepository userDeviceRepository;
    private final DeviceRepository deviceRepository;
    private final UserDeviceMapper userDeviceMapper;

    @Transactional
    public UserDeviceResponse createUserDevice(UserDeviceRequest request) {
        log.info("Creating/updating UserDevice for userId: {} with token: {}", request.getUserId(), request.getToken());

        // 1. Cerca il device per token. Se non esiste, lo crea.
        DeviceEntity device = deviceRepository.findByDeviceToken(request.getToken())
                .orElseGet(() -> {
                    log.info("Device con token {} non trovato, creazione nuovo device.", request.getToken());
                    DeviceEntity newDevice = new DeviceEntity();
                    newDevice.setDeviceToken(request.getToken());
                    newDevice.setPlatform(request.getPlatform());
                    newDevice.setCreatedAt(LocalDateTime.now());
                    newDevice.setUpdatedAt(LocalDateTime.now());
                    return deviceRepository.save(newDevice);
                });

        // 2. Cerca la relazione user-device
        UserDevice userDevice = userDeviceRepository
                .findByUserIdAndDevice_Id(request.getUserId(), device.getId())
                .orElseGet(() -> {
                    log.info("Relazione user-device non trovata, creazione nuova.");
                    UserDevice newUserDevice = new UserDevice();
                    newUserDevice.setUserId(request.getUserId());
                    newUserDevice.setDevice(device);
                    return newUserDevice;
                });

        userDevice.setLastSeenAt(LocalDateTime.now());
        userDevice.setIsActive(true);

        UserDevice saved = userDeviceRepository.save(userDevice);

        // 3. Disattiva tutte le altre associazioni dello stesso utente
        userDeviceRepository.deactivateOtherDevicesForUser(request.getUserId(), device.getId());

        log.info("UserDevice salvato con id: {}. Altre associazioni dell'utente {} disattivate.", saved.getId(), request.getUserId());

        return userDeviceMapper.toResponse(saved);
    }

    public List<UserDeviceResponse> findAll() {
        log.info("Finding all UserDevices");
        return userDeviceRepository.findAll().stream()
                .map(userDeviceMapper::toResponse)
                .collect(Collectors.toList());
    }

    public UserDeviceResponse findById(Long id) {
        log.info("Finding UserDevice with id: {}", id);
        return userDeviceMapper.toResponse(
                userDeviceRepository.findById(id)
                        .orElseThrow(() -> UserDeviceNotFoundException.create(id)));
    }

    public List<UserDeviceResponse> findAllByUserId(String userId) {
        log.info("Finding all UserDevices for userId: {}", userId);
        return userDeviceRepository.findAllByUserId(userId)
                .stream()
                .map(userDeviceMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserDeviceResponse updateUserDevice(Long id, UserDeviceRequest request) {
        log.info("Updating UserDevice with id: {}", id);

        UserDevice userDevice = userDeviceRepository.findById(id)
                .orElseThrow(() -> UserDeviceNotFoundException.create(id));

        if (request.getToken() != null) {
            DeviceEntity device = deviceRepository.findByDeviceToken(request.getToken())
                    .orElseThrow(() -> new RuntimeException("Device con token " + request.getToken() + " non trovato"));
            userDevice.setDevice(device);
        }

        if (request.getUserId() != null) {
            userDevice.setUserId(request.getUserId());
        }

        return userDeviceMapper.toResponse(userDeviceRepository.save(userDevice));
    }

    @Transactional
    public void deleteUserDevice(Long id) {
        log.info("Deleting UserDevice with id: {}", id);
        if (!userDeviceRepository.existsById(id)) {
            throw UserDeviceNotFoundException.create(id);
        }
        userDeviceRepository.deleteById(id);
    }

    @Transactional
    public void logout(UserDeviceRequest request) {
        log.info("Logout requested for userId: {} and token: {}", request.getUserId(), request.getToken());

        java.util.Optional<DeviceEntity> deviceOpt = deviceRepository.findByDeviceToken(request.getToken());
        if (deviceOpt.isEmpty()) {
            log.warn("Device con token {} non trovato. Nessuna azione necessaria per il logout.", request.getToken());
            return;
        }

        DeviceEntity device = deviceOpt.get();
        java.util.Optional<UserDevice> userDeviceOpt = userDeviceRepository.findByUserIdAndDevice_Id(request.getUserId(), device.getId());
        if (userDeviceOpt.isEmpty()) {
            log.warn("Relazione user-device non trovata per userId {} e deviceId {}. Nessuna azione necessaria per il logout.", request.getUserId(), device.getId());
            return;
        }

        UserDevice userDevice = userDeviceOpt.get();
        userDevice.setIsActive(false);
        userDeviceRepository.save(userDevice);
        log.info("UserDevice (userId: {}, deviceId: {}) successfully logged out (isActive set to false).", request.getUserId(), device.getId());
    }
}
