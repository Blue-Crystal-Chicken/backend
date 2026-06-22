package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.service;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.mapper.AddressMapper;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request.AddressRequest;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.response.AddressResponse;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.address.Address;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.address.AddressType;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AddressService {

    private final AddressRepository addressRepository;
    private final AddressMapper addressMapper;

    // ── READ ────────────────────────────────────────────────────────────────

    public List<AddressResponse> findAll() {
        log.info("Fetching all addresses");
        return addressRepository.findAll().stream()
                .map(addressMapper::toResponse)
                .collect(Collectors.toList());
    }

    public AddressResponse findResponseById(Long id) {
        return addressMapper.toResponse(findById(id));
    }

    public Address findById(Long id) {
        log.info("Finding address by id: {}", id);
        return addressRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Address not found with id: {}", id);
                    return new RuntimeException("Indirizzo non trovato con id: " + id);
                });
    }

    // ── WRITE ───────────────────────────────────────────────────────────────

    @Transactional
    public AddressResponse create(AddressRequest request) {
        log.info("Creating new address: {} {}", request.getStreet(), request.getCity());
        Address address = addressMapper.toEntity(request);
        Address saved = addressRepository.save(address);
        log.info("Address created successfully with id: {}", saved.getId());
        return addressMapper.toResponse(saved);
    }

    @Transactional
    public AddressResponse update(Long id, AddressRequest request) {
        log.info("Updating address with id: {}", id);
        Address existing = findById(id);
        
        existing.setType(request.getType() != null ? AddressType.parsingSicuro(request.getType()) : null);
        existing.setStreet(request.getStreet());
        existing.setCity(request.getCity());
        existing.setState(request.getState());
        existing.setZipCode(request.getZipCode());
        existing.setCountry(request.getCountry());
        
        Address saved = addressRepository.save(existing);
        log.info("Address updated successfully with id: {}", saved.getId());
        return addressMapper.toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        log.info("Deleting address with id: {}", id);
        if (!addressRepository.existsById(id)) {
            log.error("Attempted to delete non-existent address with id: {}", id);
            throw new RuntimeException("Indirizzo non trovato con id: " + id);
        }
        addressRepository.deleteById(id);
        log.info("Address deleted successfully: {}", id);
    }
}
