package com.r2u.notification.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

import org.springframework.stereotype.Service;

import com.r2u.notification.dto.mapper.DeviceMapper;
import com.r2u.notification.dto.request.DeviceRequest;
import com.r2u.notification.dto.response.DeviceResponse;
import com.r2u.notification.entity.model.DeviceEntity;
import com.r2u.notification.exception.DeviceNotFoundException;
import com.r2u.notification.repository.DeviceRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.transaction.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final DeviceMapper deviceMapper;

    @Transactional
    public DeviceResponse createDevice(DeviceRequest deviceRequest) {
        log.info("Creating or updating device: {}", deviceRequest);

        Optional<DeviceEntity> existingDevice = deviceRepository.findByDeviceToken(deviceRequest.getToken());

        if (existingDevice.isPresent()) {
            // Il device esiste: aggiorna solo la piattaforma
            DeviceEntity device = existingDevice.get();
            device.setPlatform(deviceRequest.getPlatform());
            device.setUpdatedAt(LocalDateTime.now());
            return deviceMapper.toResponse(deviceRepository.save(device));
        } else {
            // Nuovo device: crea da zero
            return deviceMapper.toResponse(deviceRepository.save(deviceMapper.toEntity(deviceRequest)));
        }
    }

    public List<DeviceResponse> findAll() {
        log.info("Finding all devices");
        return deviceRepository.findAll().stream()
                .map(deviceMapper::toResponse)
                .toList();
    }

    public DeviceResponse findById(Long id) {
        log.info("Finding device with id: {}", id);
        return deviceMapper.toResponse(
                deviceRepository.findById(id)
                        .orElseThrow(() -> DeviceNotFoundException.create(id)));
    }

    @Transactional
    public DeviceResponse updateDevice(Long id, DeviceRequest deviceRequest) {
        log.info("Updating device with id: {}", id);
        DeviceEntity deviceEntity = deviceRepository.findById(id)
                .orElseThrow(() -> DeviceNotFoundException.create(id));
        deviceEntity.setDeviceToken(deviceRequest.getToken());
        deviceEntity.setPlatform(deviceRequest.getPlatform());
        deviceEntity.setUpdatedAt(LocalDateTime.now());
        return deviceMapper.toResponse(deviceRepository.save(deviceEntity));
    }

    @Transactional
    public void deleteDevice(Long id) {
        log.info("Deleting device with id: {}", id);
        if (!deviceRepository.existsById(id)) {
            throw DeviceNotFoundException.create(id);
        }
        deviceRepository.deleteById(id);
    }
}
