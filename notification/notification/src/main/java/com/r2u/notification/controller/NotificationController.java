package com.r2u.notification.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.r2u.notification.dto.request.NotificationRequest;
import com.r2u.notification.dto.response.NotificationResponse;
import com.r2u.notification.exception.NotificationNotFoundException;
import com.r2u.notification.service.NotificationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {
    
    private final NotificationService notificationService;

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new notification", description = "Creates a new notification and returns the created notification")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Notification created successfully", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<NotificationResponse> createNotification(
            @RequestBody NotificationRequest notificationRequest) {
        log.info("POST /api/notification/create request: {}", notificationRequest);
        try {
            NotificationResponse notificationResponse = notificationService.createNotification(notificationRequest);
            log.info("POST /api/notification/create response: {}", notificationResponse);
            return ResponseEntity.ok(notificationResponse);
        } catch (Exception e) {
            log.error("POST /api/notification/create error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get all notifications", description = "Returns a list of all notifications")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notifications found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<List<NotificationResponse>> findAll() {
        log.info("GET /api/notification request");
        try {
            return ResponseEntity.ok(notificationService.findAll());
        } catch (Exception e) {
            log.error("GET /api/notification error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get a notification by ID", description = "Returns the notification with the specified ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notification found", content = @Content),
            @ApiResponse(responseCode = "404", description = "Notification not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<NotificationResponse> getNotificationById(@PathVariable Long id) {
        log.info("GET /api/notification/{} request: {}", id);
        try {
            NotificationResponse notificationResponse = notificationService.getNotificationById(id);
            log.info("GET /api/notification/{} response: {}", id, notificationResponse);
            return ResponseEntity.ok(notificationResponse);
        } catch (NotificationNotFoundException e) {
            log.error("GET /api/notification/{} error: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            log.error("GET /api/notification/{} error: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Update a notification by ID", description = "Updates the notification with the specified ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notification updated successfully", content = @Content),
            @ApiResponse(responseCode = "404", description = "Notification not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<NotificationResponse> updateNotification(@PathVariable Long id, @RequestBody NotificationRequest notificationRequest) {
        log.info("PUT /api/notification/{} request: {}", id, notificationRequest);
        try {
            NotificationResponse notificationResponse = notificationService.updateNotification(id, notificationRequest);
            return ResponseEntity.ok(notificationResponse);
        } catch (NotificationNotFoundException e) {
            log.error("PUT /api/notification/{} error: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            log.error("PUT /api/notification/{} error: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Delete a notification by ID", description = "Deletes the notification with the specified ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notification deleted successfully", content = @Content),
            @ApiResponse(responseCode = "404", description = "Notification not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        log.info("DELETE /api/notification/{} request", id);
        try {
            notificationService.deleteNotification(id);
            return ResponseEntity.ok().build();
        } catch (NotificationNotFoundException e) {
            log.error("DELETE /api/notification/{} error: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("DELETE /api/notification/{} error: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
