package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.Role;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.UserEntity;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.exception.EmailAlreadyExistsException;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.exception.EmailNotFoundException;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.exception.UserNotFoundException;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.repository.UserRepository;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.utils.PasswordHasher;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils; 

    @Transactional(readOnly = true)
    public ResponseEntity<?> login(LoginRequest request) {
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
                userDetails.getUsername(), 
                roles));
    }

    @Transactional
    public ResponseEntity<?> registerUser(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("Email already exists");
        }

        UserEntity user = new UserEntity();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(PasswordHasher.hashPassword(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        user.setRole(request.getRole());

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
        existingUser.setPassword(PasswordHasher.hashPassword(user.getPassword()));
        return ResponseEntity.ok(userRepository.save(existingUser));
    }

    @Transactional
    public ResponseEntity<?> deleteUser(Long id) {
        userRepository.deleteById(id);
        return ResponseEntity.ok("User deleted successfully");
    }


    
}
