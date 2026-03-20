package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.payload.request.LoginRequest;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.payload.request.RegisterRequest;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.service.UserService;


@RestController
@RequestMapping("/api/auth")
public class UserAuthController {
    
    @Autowired
    private UserService userService;
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest user) {
        try {
            return ResponseEntity.ok(userService.login(user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest user) {
        try {
            return ResponseEntity.ok(userService.registerUser(user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}