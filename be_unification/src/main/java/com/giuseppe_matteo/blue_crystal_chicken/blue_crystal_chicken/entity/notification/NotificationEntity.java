package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.notification;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Notifica-log persistita: viene scritta dal consumer RabbitMQ a partire
 * dagli eventi pubblicati dai service/controller.
 */
@Entity
@Table(name = "NOTIFICATIONS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // Tipo evento (es. OFFER_CREATED, PRODUCT_UPDATED, LOW_STOCK, LOCATION_OPENED)
    @Column(name = "Type")
    private String type;

    // Severità per la UI: INFO, SUCCESS, WARNING, ERROR
    @Column(name = "Level")
    private String level;

    // Dominio di origine: Marketing, Magazzino, Sedi, Prodotti
    @Column(name = "Source")
    private String source;

    @Column(name = "Title")
    private String title;

    @Column(name = "Message", length = 512)
    private String message;

    // Sede destinataria (null = globale, visibile a tutti). Usata per indirizzare
    // le notifiche al Manager della sede (es. chiusura sede).
    @Column(name = "Location_id")
    private Long locationId;

    @Column(name = "Is_read")
    private Boolean readFlag = Boolean.FALSE;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (readFlag == null) readFlag = Boolean.FALSE;
    }
}
