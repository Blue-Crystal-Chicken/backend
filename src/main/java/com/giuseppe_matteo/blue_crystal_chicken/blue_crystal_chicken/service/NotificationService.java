package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.service;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.notification.NotificationEntity;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public List<NotificationEntity> findAll() {
        return notificationRepository.findAllByOrderByCreatedAtDesc();
    }

    public long unreadCount() {
        return notificationRepository.countByReadFlagFalse();
    }

    @Transactional
    public NotificationEntity markRead(Long id) {
        NotificationEntity n = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notifica non trovata: " + id));
        n.setReadFlag(Boolean.TRUE);
        return notificationRepository.save(n);
    }

    @Transactional
    public void markAllRead() {
        List<NotificationEntity> unread = notificationRepository.findAll();
        unread.forEach(n -> n.setReadFlag(Boolean.TRUE));
        notificationRepository.saveAll(unread);
    }

    @Transactional
    public void delete(Long id) {
        notificationRepository.deleteById(id);
    }

    @Transactional
    public void deleteAll() {
        notificationRepository.deleteAll();
    }
}
