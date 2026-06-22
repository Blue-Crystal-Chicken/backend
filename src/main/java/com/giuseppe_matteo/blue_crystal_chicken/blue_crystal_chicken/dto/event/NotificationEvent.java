package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Payload pubblicato su RabbitMQ ad ogni evento di dominio.
 * Serializzato in JSON (Jackson) sull'exchange "bcc.events".
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent implements Serializable {
    private String type;     // OFFER_CREATED, PRODUCT_UPDATED, LOW_STOCK, ...
    private String level;    // INFO | SUCCESS | WARNING | ERROR
    private String source;   // Marketing | Magazzino | Sedi | Prodotti
    private String title;
    private String message;
    private Long locationId; // sede destinataria (null = globale, visibile a tutti)
}
