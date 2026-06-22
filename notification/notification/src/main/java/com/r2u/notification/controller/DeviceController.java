package com.r2u.notification.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.r2u.notification.dto.request.DeviceRequest;
import com.r2u.notification.dto.response.DeviceResponse;
import com.r2u.notification.service.DeviceService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/devices")
@Slf4j
public class DeviceController {
    
    private final DeviceService deviceService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new device", description = "Creates a new device and returns the created device")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Device created successfully", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<DeviceResponse> createDevice(@RequestBody DeviceRequest deviceRequest) {
        log.info("POST /devices with body: {}", deviceRequest);
        return ResponseEntity.ok(deviceService.createDevice(deviceRequest));
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get all devices", description = "Returns a list of all devices")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Devices found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<List<DeviceResponse>> findAll() {
        log.info("GET /devices");
        return ResponseEntity.ok(deviceService.findAll());
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get a device by ID", description = "Returns the device with the specified ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Device found", content = @Content),
            @ApiResponse(responseCode = "404", description = "Device not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<DeviceResponse> findById(@PathVariable Long id) {
        log.info("GET /devices/{} with id: {}", id);
        try {
            return ResponseEntity.ok(deviceService.findById(id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Update a device by ID", description = "Updates the device with the specified ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Device updated successfully", content = @Content),
            @ApiResponse(responseCode = "404", description = "Device not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<DeviceResponse> updateDevice(@PathVariable Long id, @RequestBody DeviceRequest deviceRequest) {
        log.info("PUT /devices/{} with body: {}", id, deviceRequest);
        return ResponseEntity.ok(deviceService.updateDevice(id, deviceRequest));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Delete a device by ID", description = "Deletes the device with the specified ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Device deleted successfully", content = @Content),
            @ApiResponse(responseCode = "404", description = "Device not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<Void> deleteDevice(@PathVariable Long id) {
        log.info("DELETE /devices/{} with id: {}", id);
        deviceService.deleteDevice(id);
        return ResponseEntity.ok().build();
    }
    
}
