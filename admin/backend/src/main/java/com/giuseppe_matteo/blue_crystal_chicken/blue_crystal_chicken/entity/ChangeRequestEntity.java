package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Richiesta di modifica inoltrata da un Manager e in attesa di approvazione
 * dell'Admin. Finché è PENDING NON viene scritta nulla sul dominio reale: solo
 * all'approvazione l'Admin esegue la mutazione corrispondente al payload.
 */
@Entity
@Table(name = "CHANGE_REQUESTS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangeRequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "Type", nullable = false)
    private ChangeRequestType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", nullable = false)
    private ChangeRequestStatus status;

    /** Corpo della create serializzato in JSON (ProductRequest / OfferRequest). */
    @Column(name = "Payload", columnDefinition = "TEXT")
    private String payload;

    /** Id dell'entità su cui agire (per UPDATE/DELETE). Null per le create. */
    @Column(name = "Target_id")
    private Long targetId;

    /** Descrizione leggibile mostrata all'admin (es. "Nuovo prodotto: Crystal Burger"). */
    @Column(name = "Summary")
    private String summary;

    // Chi richiede (manager) + la sua sede
    @Column(name = "Requested_by_id")
    private Long requestedById;

    @Column(name = "Requested_by_email")
    private String requestedByEmail;

    @Column(name = "Location_id")
    private Long locationId;

    @Column(name = "Location_name")
    private String locationName;

    // Esito
    @Column(name = "Resolved_by_id")
    private Long resolvedById;

    @Column(name = "Resolution_note", length = 512)
    private String resolutionNote;

    @Column(name = "Result_message", length = 512)
    private String resultMessage;

    @Column(name = "Created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "Resolved_at")
    private LocalDateTime resolvedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) status = ChangeRequestStatus.PENDING;
    }
}
