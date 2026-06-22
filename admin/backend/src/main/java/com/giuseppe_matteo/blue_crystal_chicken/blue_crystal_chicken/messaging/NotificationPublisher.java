package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.messaging;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.config.RabbitConfig;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.event.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Pubblica gli eventi di dominio sull'exchange "bcc.events".
 * La pubblicazione è resiliente: se il broker non è raggiungibile l'errore
 * viene loggato ma NON propagato, così le operazioni applicative non falliscono.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publish(String routingKey, NotificationEvent event) {
        try {
            rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, routingKey, event);
            log.debug("Event published [{}]: {}", routingKey, event.getTitle());
        } catch (Exception e) {
            log.warn("Pubblicazione evento fallita (broker non disponibile?) [{}]: {}", routingKey, e.getMessage());
        }
    }

    // Helper compatto per i call site (notifica globale)
    public void publish(String routingKey, String type, String level, String source, String title, String message) {
        publish(routingKey, NotificationEvent.builder()
                .type(type).level(level).source(source).title(title).message(message).build());
    }

    // Helper con sede destinataria (notifica indirizzata al Manager di quella sede)
    public void publish(String routingKey, String type, String level, String source, String title, String message, Long locationId) {
        publish(routingKey, NotificationEvent.builder()
                .type(type).level(level).source(source).title(title).message(message).locationId(locationId).build());
    }
}
