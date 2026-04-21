package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request.Login;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request.Register;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.response.JwtResponse;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.UserEntity;
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


@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils; 
    

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

        return ResponseEntity.ok(new JwtResponse(jwt, 
                userDetails.getId(), 
                userDetails.getEmail(), 
                userDetails.getName(),
                userDetails.getSurname(),
                userDetails.getPhone(),
                userDetails.getGender(),
                userDetails.getBirthday(),
                roles));
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

        // Return user data without a new token (or we could generate a new one if needed, 
        // but for session restoration, the old token is still valid)
        return ResponseEntity.ok(new JwtResponse(null, 
                userDetails.getId(), 
                userDetails.getEmail(), 
                userDetails.getName(),
                userDetails.getSurname(),
                userDetails.getPhone(),
                userDetails.getGender(),
                userDetails.getBirthday(),
                roles));
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

        userRepository.save(user);

        return ResponseEntity.ok("User registered successfully");
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
    public ResponseEntity<?> updateUser(Long id, UserEntity user) {
        UserEntity existingUser = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException());
        existingUser.setEmail(user.getEmail());
        existingUser.setPassword(PasswordHasher.hash(user.getPassword()));
        existingUser.setPhone(user.getPhone());
        existingUser.setGender(user.getGender());
        existingUser.setBirthday(user.getBirthday());
        existingUser.setRole(user.getRole());
        return ResponseEntity.ok(userRepository.save(existingUser));
    }

    @Transactional
    public ResponseEntity<?> deleteUser(Long id) {
        userRepository.deleteById(id);
        return ResponseEntity.ok("User deleted successfully");
    }


    
}
