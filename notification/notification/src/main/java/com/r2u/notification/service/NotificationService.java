package com.r2u.notification.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.r2u.notification.dto.mapper.NotificationMapper;
import com.r2u.notification.dto.request.NotificationRequest;
import com.r2u.notification.dto.response.NotificationResponse;
import com.r2u.notification.entity.model.NotificationEntity;
import com.r2u.notification.exception.NotificationNotFoundException;
import com.r2u.notification.repository.NotificationRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    @Transactional
    public NotificationResponse createNotification(NotificationRequest notificationRequest) {
        log.info("Creating notification: {}", notificationRequest);
        return notificationMapper.toResponse(notificationRepository.save(notificationMapper.toEntity(notificationRequest)));
    }

    public List<NotificationResponse> findAll() {
        log.info("Finding all notifications");
        return notificationRepository.findAll().stream()
                .map(notificationMapper::toResponse)
                .toList();
    }

    public NotificationResponse getNotificationById(Long id) {
        log.info("Getting notification with id: {}", id);
        return notificationMapper.toResponse(notificationRepository.findById(id).orElseThrow(() -> NotificationNotFoundException.create(id)));
    }

    @Transactional
    public NotificationResponse updateNotification(Long id, NotificationRequest notificationRequest) {
        log.info("Updating notification with id: {}", id);
        NotificationEntity notificationEntity = notificationRepository.findById(id)
                .orElseThrow(() -> NotificationNotFoundException.create(id));
        
        notificationEntity.setTitle(notificationRequest.getTitle());
        notificationEntity.setBody(notificationRequest.getBody());
        if (notificationRequest.getType() != null) {
            notificationEntity.setType(com.r2u.notification.entity.enums.Type.valueOf(notificationRequest.getType().toUpperCase()));
        }
        if (notificationRequest.getPayload() != null) {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            notificationEntity.setPayload(mapper.valueToTree(notificationRequest.getPayload()));
        }
        
        return notificationMapper.toResponse(notificationRepository.save(notificationEntity));
    }

    @Transactional
    public void deleteNotification(Long id) {
        log.info("Deleting notification with id: {}", id);
        if (!notificationRepository.existsById(id)) {
            throw NotificationNotFoundException.create(id);
        }
        notificationRepository.deleteById(id);
    }
}
