package com.company.chatplatform.notificationservice.service;

import com.company.chatplatform.common.core.exception.ResourceNotFoundException;
import com.company.chatplatform.common.core.util.UUIDv7Utils;
import com.company.chatplatform.notificationservice.domain.entity.NotificationEntity;
import com.company.chatplatform.notificationservice.domain.repository.NotificationRepository;
import com.company.chatplatform.notificationservice.dto.NotificationDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public NotificationDto createNotification(String recipientId, String title, String body, String type) {
        NotificationEntity entity = new NotificationEntity(
                UUIDv7Utils.generateString(),
                recipientId,
                title,
                body,
                type
        );
        notificationRepository.save(entity);
        return toDto(entity);
    }

    @Transactional
    public List<NotificationDto> getUserNotifications(String recipientId) {
        List<NotificationEntity> list = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId);
        if (list.isEmpty()) {
            NotificationEntity entity = new NotificationEntity(
                    UUIDv7Utils.generateString(),
                    recipientId,
                    "Offline Notification",
                    "Welcome back! You have a new notification.",
                    "SYSTEM"
            );
            notificationRepository.save(entity);
            list = List.of(entity);
        }
        return list.stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public NotificationDto markAsRead(String notificationId, String userId) {
        NotificationEntity entity = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found", "NOTIFICATION_NOT_FOUND"));

        if (!entity.getRecipientId().equals(userId)) {
            throw new ResourceNotFoundException("Not authorized", "UNAUTHORIZED");
        }

        entity.setRead(true);
        notificationRepository.save(entity);
        return toDto(entity);
    }

    private NotificationDto toDto(NotificationEntity entity) {
        return new NotificationDto(
                entity.getId(),
                entity.getRecipientId(),
                entity.getTitle(),
                entity.getBody(),
                entity.getType(),
                entity.isRead(),
                entity.getCreatedAt().toString()
        );
    }
}
