package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.service;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.LocationEntity;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final LocationRepository locationRepository;

    // ── READ ────────────────────────────────────────────────────────────────

    public List<LocationEntity> findAll() {
        return locationRepository.findAll();
    }

    public LocationEntity findById(Long id) {
        return locationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Location non trovata con id: " + id));
    }

    public List<LocationEntity> findByCity(String city) {
        return locationRepository.findByCity(city);
    }

    public List<LocationEntity> findByStatus(String status) {
        return locationRepository.findByStatus(status);
    }

    // ── WRITE ───────────────────────────────────────────────────────────────

    @Transactional
    public LocationEntity create(LocationEntity location) {
        if (locationRepository.existsByName(location.getName())) {
            throw new RuntimeException("Location con nome '" + location.getName() + "' già esistente");
        }
        return locationRepository.save(location);
    }

    @Transactional
    public LocationEntity update(Long id, LocationEntity updated) {
        LocationEntity existing = findById(id);
        existing.setName(updated.getName());
        existing.setAddress(updated.getAddress());
        existing.setCity(updated.getCity());
        existing.setPhoneCode(updated.getPhoneCode());
        existing.setPhoneNumber(updated.getPhoneNumber());
        existing.setIsOpen(updated.getIsOpen());
        existing.setStatus(updated.getStatus());
        return locationRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        if (!locationRepository.existsById(id)) {
            throw new RuntimeException("Location non trovata con id: " + id);
        }
        locationRepository.deleteById(id);
    }
}
