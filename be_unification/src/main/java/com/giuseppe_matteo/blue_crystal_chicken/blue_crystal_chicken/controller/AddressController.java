package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.controller;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request.AddressRequest;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.response.AddressResponse;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.service.AddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
@Slf4j
public class AddressController {

    private final AddressService addressService;

    // GET /api/addresses
    @GetMapping
    public ResponseEntity<List<AddressResponse>> getAll() {
        log.info("REST request to get all addresses");
        return ResponseEntity.ok(addressService.findAll());
    }

    // GET /api/addresses/{id}
    @GetMapping("/{id}")
    public ResponseEntity<AddressResponse> getById(@PathVariable Long id) {
        log.info("REST request to get address by id: {}", id);
        return ResponseEntity.ok(addressService.findResponseById(id));
    }

    // POST /api/addresses
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AddressResponse> create(@RequestBody AddressRequest request) {
        log.info("REST request to create address: {} {}", request.getStreet(), request.getCity());
        return ResponseEntity.status(HttpStatus.CREATED).body(addressService.create(request));
    }

    // PUT /api/addresses/{id}
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AddressResponse> update(@PathVariable Long id, @RequestBody AddressRequest request) {
        log.info("REST request to update address: {}", id);
        return ResponseEntity.ok(addressService.update(id, request));
    }

    // DELETE /api/addresses/{id}
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("REST request to delete address: {}", id);
        addressService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
