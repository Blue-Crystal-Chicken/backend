package com.r2u.notification.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.r2u.notification.dto.request.UserDeviceRequest;
import com.r2u.notification.dto.response.UserDeviceResponse;
import com.r2u.notification.service.UserDeviceService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user-devices")
@Slf4j
public class UserDeviceController {

    private final UserDeviceService userDeviceService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new user-device association", description = "Associates a user with a device and returns the created record")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "UserDevice created successfully", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "404", description = "Device not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<UserDeviceResponse> createUserDevice(@RequestBody UserDeviceRequest request) {
        log.info("POST /user-devices with body: {}", request);
        return ResponseEntity.status(HttpStatus.CREATED).body(userDeviceService.createUserDevice(request));
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get all user-devices", description = "Returns all user-device associations")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "UserDevices found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<List<UserDeviceResponse>> findAll() {
        log.info("GET /user-devices");
        return ResponseEntity.ok(userDeviceService.findAll());
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get a user-device by ID", description = "Returns the user-device association with the specified ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "UserDevice found", content = @Content),
            @ApiResponse(responseCode = "404", description = "UserDevice not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<UserDeviceResponse> findById(@PathVariable Long id) {
        log.info("GET /user-devices/{}", id);
        try {
            return ResponseEntity.ok(userDeviceService.findById(id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/user/{userId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get all devices for a user", description = "Returns all user-device associations for the given userId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List returned successfully", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<List<UserDeviceResponse>> findAllByUserId(@PathVariable String userId) {
        log.info("GET /user-devices/user/{}", userId);
        return ResponseEntity.ok(userDeviceService.findAllByUserId(userId));
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Update a user-device by ID", description = "Updates the user-device association with the specified ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "UserDevice updated successfully", content = @Content),
            @ApiResponse(responseCode = "404", description = "UserDevice not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<UserDeviceResponse> updateUserDevice(@PathVariable Long id, @RequestBody UserDeviceRequest request) {
        log.info("PUT /user-devices/{} with body: {}", id, request);
        return ResponseEntity.ok(userDeviceService.updateUserDevice(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Delete a user-device by ID", description = "Deletes the user-device association with the specified ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "UserDevice deleted successfully", content = @Content),
            @ApiResponse(responseCode = "404", description = "UserDevice not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<Void> deleteUserDevice(@PathVariable Long id) {
        log.info("DELETE /user-devices/{}", id);
        userDeviceService.deleteUserDevice(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Logout a user-device", description = "Sets the user-device association as inactive")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logout successful", content = @Content),
            @ApiResponse(responseCode = "404", description = "UserDevice not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<Void> logout(@RequestBody UserDeviceRequest request) {
        log.info("POST /user-devices/logout with body: {}", request);
        userDeviceService.logout(request);
        return ResponseEntity.ok().build();
    }
    
}
