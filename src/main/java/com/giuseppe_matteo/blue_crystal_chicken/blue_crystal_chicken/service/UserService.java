package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request.Login;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request.Register;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request.UserUpdateRequest;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.response.JwtResponse;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.user.UserEntity;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.exception.EmailNotFoundException;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.exception.UserNotFoundException;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.repository.UserRepository;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.security.jwt.JwtUtils;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.security.servicies.UserDetailsImpl;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.utils.PasswordHasher;

import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.mapper.AddressMapper;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.mapper.LocationMapper;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.response.LocationResponse;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.response.AddressResponse;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.address.Address;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.address.AddressType;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.location.LocationEntity;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.repository.LocationRepository;


@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private LocationMapper locationMapper;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private AddressMapper addressMapper;
    
    

    @Transactional(readOnly = true)
    public ResponseEntity<?> login(Login request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        UserEntity user = userRepository.findById(userDetails.getId()).orElse(null);
        LocationResponse locationResponse = null;
        if (user != null && user.getLocation() != null) {
            locationResponse = locationMapper.toResponse(user.getLocation());
        }
        AddressResponse addressResponse = null;
        if (user != null && user.getAddress() != null) {
            addressResponse = addressMapper.toResponse(user.getAddress());
        }

        JwtResponse resp = new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getEmail(),
                userDetails.getName(),
                userDetails.getSurname(),
                userDetails.getPhone(),
                userDetails.getGender(),
                userDetails.getBirthday(),
                roles,
                locationResponse,
                addressResponse);
        if (user != null && user.getLocation() != null) {
            resp.setLocationId(user.getLocation().getId());
            resp.setLocationName(user.getLocation().getName());
        }
        return ResponseEntity.ok(resp);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<?> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.status(401).body("Not authenticated");
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        UserEntity user = userRepository.findById(userDetails.getId()).orElse(null);
        LocationResponse locationResponse = null;
        if (user != null && user.getLocation() != null) {
            locationResponse = locationMapper.toResponse(user.getLocation());
        }
        AddressResponse addressResponse = null;
        if (user != null && user.getAddress() != null) {
            addressResponse = addressMapper.toResponse(user.getAddress());
        }

        // Return user data without a new token (or we could generate a new one if needed, 
        // but for session restoration, the old token is still valid)
        JwtResponse resp = new JwtResponse(null,
                userDetails.getId(),
                userDetails.getEmail(),
                userDetails.getName(),
                userDetails.getSurname(),
                userDetails.getPhone(),
                userDetails.getGender(),
                userDetails.getBirthday(),
                roles,
                locationResponse,
                addressResponse);
        if (user != null && user.getLocation() != null) {
            resp.setLocationId(user.getLocation().getId());
            resp.setLocationName(user.getLocation().getName());
        }
        return ResponseEntity.ok(resp);
    }


    @Transactional
    public ResponseEntity<?> registerUser(Register request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("Email already exists");
        }

        UserEntity user = new UserEntity();
        user.setName(request.getName());
        user.setSurname(request.getSurname());
        user.setEmail(request.getEmail());
        user.setPassword(PasswordHasher.hash(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setRole(request.getRole());
        user.setGender(request.getGender());
        user.setBirthday(LocalDate.parse(request.getBirthday()));

        if (request.getAddress() != null) {
            user.setAddress(addressMapper.toEntity(request.getAddress()));
        }

        userRepository.save(user);

        return login(new Login(request.getEmail(), request.getPassword()));
    }

    @Transactional(readOnly = true)
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @Transactional(readOnly = true)
    public ResponseEntity<?> getUserById(Long id) {
        return ResponseEntity.ok(userRepository.findById(id).orElseThrow(() -> new UserNotFoundException()));
    }

    @Transactional(readOnly = true)
    public ResponseEntity<?> getUserByEmail(String email) {
        return ResponseEntity.ok(userRepository.findByEmail(email).orElseThrow(() -> new EmailNotFoundException()));
    }

    @Transactional
    public ResponseEntity<?> updateUser(Long id, UserUpdateRequest request) {
        UserEntity existingUser = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException());
        existingUser.setName(request.getName());
        existingUser.setSurname(request.getSurname());
        existingUser.setEmail(request.getEmail());
        existingUser.setPhone(request.getPhone());
        existingUser.setGender(request.getGender());
        existingUser.setBirthday(LocalDate.parse(request.getBirthday()));

        if (request.getAddress() != null) {
            if (existingUser.getAddress() != null) {
                Address addr = existingUser.getAddress();
                addr.setType(request.getAddress().getType() != null ? AddressType.parsingSicuro(request.getAddress().getType()) : addr.getType());
                addr.setStreet(request.getAddress().getStreet());
                addr.setCity(request.getAddress().getCity());
                addr.setState(request.getAddress().getState());
                addr.setZipCode(request.getAddress().getZipCode());
                addr.setCountry(request.getAddress().getCountry());
            } else {
                existingUser.setAddress(addressMapper.toEntity(request.getAddress()));
            }
        } else {
            existingUser.setAddress(null);
        }

        return ResponseEntity.ok(userRepository.save(existingUser));
    }

    @Transactional
    public ResponseEntity<?> deleteUser(Long id) {
        userRepository.deleteById(id);
        return ResponseEntity.ok("User deleted successfully");
    }

    @Transactional
    public ResponseEntity<?> updateUserLocation(Long id, Long locationId) {
        UserEntity existingUser = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException());
        LocationEntity location = locationRepository.findById(locationId)
                .orElseThrow(() -> new RuntimeException("Location not found"));
        existingUser.setLocation(location);
        UserEntity savedUser = userRepository.save(existingUser);
        return ResponseEntity.ok(savedUser);
    }
}
