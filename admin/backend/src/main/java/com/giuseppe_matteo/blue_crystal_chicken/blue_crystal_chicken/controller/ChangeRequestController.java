package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.controller;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request.CreateChangeRequest;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.response.ChangeRequestResponse;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.ChangeRequestStatus;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.security.servicies.UserDetailsImpl;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.service.ChangeRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
public class ChangeRequestController {

    private final ChangeRequestService service;

    // POST /api/requests — il Manager (o Admin) inoltra una richiesta di modifica
    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<?> create(@RequestBody CreateChangeRequest body,
                                    @AuthenticationPrincipal UserDetailsImpl user) {
        try {
            ChangeRequestResponse res = service.create(
                    body, user.getId(), user.getEmail(), user.getLocationId(), user.getLocationName());
            return ResponseEntity.status(HttpStatus.CREATED).body(res);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // GET /api/requests — Admin: tutte (o filtrate per ?status=PENDING)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ChangeRequestResponse>> getAll(@RequestParam(required = false) String status) {
        if (status != null && !status.isBlank()) {
            return ResponseEntity.ok(service.findByStatus(ChangeRequestStatus.valueOf(status.toUpperCase())));
        }
        return ResponseEntity.ok(service.findAll());
    }

    // GET /api/requests/mine — l'utente corrente vede le proprie richieste
    @GetMapping("/mine")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<List<ChangeRequestResponse>> getMine(@AuthenticationPrincipal UserDetailsImpl user) {
        return ResponseEntity.ok(service.findByUser(user.getId()));
    }

    // GET /api/requests/location/{id} — richieste di una sede
    @GetMapping("/location/{id}")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<List<ChangeRequestResponse>> getByLocation(@PathVariable Long id) {
        return ResponseEntity.ok(service.findByLocation(id));
    }

    // PUT /api/requests/{id}/approve — Admin approva ed esegue la create reale
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> approve(@PathVariable Long id, @AuthenticationPrincipal UserDetailsImpl user) {
        try {
            return ResponseEntity.ok(service.approve(id, user.getId()));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // PUT /api/requests/{id}/reject — Admin rifiuta (con nota opzionale)
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> reject(@PathVariable Long id,
                                    @RequestBody(required = false) Map<String, String> body,
                                    @AuthenticationPrincipal UserDetailsImpl user) {
        try {
            String note = body != null ? body.get("note") : null;
            return ResponseEntity.ok(service.reject(id, user.getId(), note));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
