package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.service;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.mapper.LocationMapper;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request.LocationRequest;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.response.LocationResponse;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.LocationEntity;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationService {

    private final LocationRepository locationRepository;
    private final LocationMapper locationMapper;

    // ── READ ────────────────────────────────────────────────────────────────

    public List<LocationResponse> findAll() {
        return locationRepository.findAll().stream()
                .map(locationMapper::toResponse)
                .collect(Collectors.toList());
    }

    public LocationResponse findLocationResponseById(Long id) {
        return locationMapper.toResponse(findById(id));
    }

    public LocationEntity findById(Long id) {
        return locationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Location non trovata con id: " + id));
    }

    public List<LocationResponse> findByCity(String city) {
        return locationRepository.findByCity(city).stream()
                .map(locationMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<LocationResponse> findByIsOpen(Boolean isOpen) {
        return locationRepository.findByIsOpen(isOpen).stream()
                .map(locationMapper::toResponse)
                .collect(Collectors.toList());
    }

    public LocationResponse setIsOpen(Long id, Boolean isOpen) {
        LocationEntity location = findById(id);
        location.setIsOpen(isOpen);
        return locationMapper.toResponse(locationRepository.save(location));
    }

    @Transactional
    public List<LocationResponse> setAllIsOpen(Boolean isOpen) {
        locationRepository.setAllIsOpen(isOpen);
        return findAll();
    }

    public List<LocationResponse> findByStatus(String status) {
        return locationRepository.findByStatus(status).stream()
                .map(locationMapper::toResponse)
                .collect(Collectors.toList());
    }

    public Integer findTablesById(Long tables){
        return locationRepository.getTablesByLocationId(tables);
    }

    // ── WRITE ───────────────────────────────────────────────────────────────

    @Transactional
    public LocationResponse create(LocationRequest request) {
        if (locationRepository.existsByName(request.getName())) {
            throw new RuntimeException("Location con nome '" + request.getName() + "' già esistente");
        }
        LocationEntity location = locationMapper.toEntity(request);
        return locationMapper.toResponse(locationRepository.save(location));
    }

    @Transactional
    public LocationResponse update(Long id, LocationRequest request) {
        LocationEntity existing = findById(id);
        
        existing.setName(request.getName());
        existing.setAddress(request.getAddress());
        existing.setCity(request.getCity());
        existing.setPhoneCode(request.getPhoneCode());
        existing.setPhoneNumber(request.getPhoneNumber());
        existing.setTables(request.getTables());
        existing.setIsOpen(request.getIsOpen());
        existing.setManuallyClosed(request.getManuallyClosed() != null ? request.getManuallyClosed() : existing.isManuallyClosed());
        existing.setStatus(request.getStatus());
        
        return locationMapper.toResponse(locationRepository.save(existing));
    }

    @Transactional
    public void delete(Long id) {
        if (!locationRepository.existsById(id)) {
            throw new RuntimeException("Location non trovata con id: " + id);
        }
        locationRepository.deleteById(id);
    }
}
