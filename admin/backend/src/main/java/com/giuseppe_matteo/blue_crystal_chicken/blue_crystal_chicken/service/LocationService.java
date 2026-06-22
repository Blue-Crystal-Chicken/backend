package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.service;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.LocationEntity;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.Role;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.UserEntity;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.repository.LocationRepository;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.repository.UserRepository;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request.LocationCreateRequest;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.utils.PasswordHasher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationService {

    private final LocationRepository locationRepository;
    private final UserRepository userRepository;

    // ── READ ────────────────────────────────────────────────────────────────

    public List<LocationEntity> findAll() {
        return locationRepository.findAll();
    }

    public LocationEntity findById(Long id) {
        return locationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Location non trovata con id: " + id));
    }

    public List<LocationEntity> findByCity(String city) {
        log.info("City: " + city);
        List<LocationEntity> locations = locationRepository.findByCity(city);
        log.info("Locations: " + locations);
        return locations;
    }

    public List<LocationEntity> findByIsOpen(Boolean isOpen) {
        return locationRepository.findByIsOpen(isOpen);
    }

    public LocationEntity setIsOpen(Long id, Boolean isOpen) {
        LocationEntity location = findById(id);
        location.setIsOpen(isOpen);
        return locationRepository.save(location);
    }

    @Transactional
    public List<LocationEntity> setAllIsOpen(Boolean isOpen) {
        locationRepository.setAllIsOpen(isOpen);
        return locationRepository.findAll();
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
        // Genera il token-stazione (per la Cucina) se non già presente.
        if (location.getStationToken() == null || location.getStationToken().isEmpty()) {
            location.setStationToken(java.util.UUID.randomUUID().toString().replace("-", ""));
        }
        return locationRepository.save(location);
    }

    /** Token-stazione di una sede (uso Admin: da configurare nella Cucina). */
    public String getStationToken(Long locationId) {
        return findById(locationId).getStationToken();
    }

    /** Sede a cui appartiene un token-stazione (usato da Cucina/Tabellone per identificarsi). */
    public LocationEntity findByStationToken(String token) {
        if (token == null || token.isBlank())
            return null;
        return locationRepository.findByStationToken(token).orElse(null);
    }

    /**
     * Crea una sede e, se presenti email+password nel DTO, crea anche l'account
     * MANAGER collegato a quella sede. Nome/cognome/telefono del manager hanno
     * default sensati se non forniti, così l'Admin deve indicare solo email e
     * password (l'email aziendale con cui il dipendente entrerà nel pannello Manager).
     */
    @Transactional
    public LocationEntity createWithManager(LocationCreateRequest req) {
        LocationEntity location = new LocationEntity();
        location.setName(req.getName());
        location.setAddress(req.getAddress());
        location.setCity(req.getCity());
        location.setPhoneNumber(req.getPhoneNumber());
        location.setIsOpen(req.getIsOpen() != null ? req.getIsOpen() : Boolean.TRUE);

        LocationEntity saved = create(location);

        // Crea il manager solo se sono state fornite email + password.
        String email = req.getManagerEmail() != null ? req.getManagerEmail().trim() : null;
        String password = req.getManagerPassword();
        if (email != null && !email.isEmpty() && password != null && !password.isEmpty()) {
            if (userRepository.existsByEmail(email)) {
                throw new RuntimeException("Esiste già un utente con email '" + email + "'");
            }
            UserEntity manager = new UserEntity();
            manager.setEmail(email);
            manager.setPassword(PasswordHasher.hash(password));
            manager.setRole(Role.MANAGER);
            manager.setLocation(saved);
            // Campi obbligatori dell'entità: default sensati se non forniti.
            String name = req.getManagerName() != null && !req.getManagerName().trim().isEmpty()
                    ? req.getManagerName().trim() : "Manager";
            String surname = req.getManagerSurname() != null && !req.getManagerSurname().trim().isEmpty()
                    ? req.getManagerSurname().trim()
                    : (req.getCity() != null && req.getCity().trim().length() >= 3 ? req.getCity().trim() : "Sede");
            String phone = req.getManagerPhone() != null && req.getManagerPhone().replaceAll("\\D", "").length() == 10
                    ? req.getManagerPhone().replaceAll("\\D", "") : "0000000000";
            manager.setName(name);
            manager.setSurname(surname);
            manager.setPhone(phone);
            manager.setGender("Other");
            manager.setBirthday(LocalDate.of(1990, 1, 1));
            userRepository.save(manager);
            log.info("Manager '{}' creato e assegnato alla sede '{}' (id {})", email, saved.getName(), saved.getId());
        }

        return saved;
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
