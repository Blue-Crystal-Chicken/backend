package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.messaging;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.config.RabbitConfig;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.event.NotificationEvent;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.NotificationEntity;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consuma gli eventi dalla coda "bcc.notifications" e li persiste come
 * notifiche-log leggibili dal frontend via REST.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final NotificationRepository notificationRepository;

    @RabbitListener(queues = RabbitConfig.NOTIFICATIONS_QUEUE)
    public void onEvent(NotificationEvent event) {
        NotificationEntity entity = new NotificationEntity();
        entity.setType(event.getType());
        entity.setLevel(event.getLevel() != null ? event.getLevel() : "INFO");
        entity.setSource(event.getSource());
        entity.setTitle(event.getTitle());
        entity.setMessage(event.getMessage());
        entity.setLocationId(event.getLocationId());
        entity.setReadFlag(Boolean.FALSE);
        notificationRepository.save(entity);
        log.info("Notification persisted from event: {}", event.getType());
    }
}
