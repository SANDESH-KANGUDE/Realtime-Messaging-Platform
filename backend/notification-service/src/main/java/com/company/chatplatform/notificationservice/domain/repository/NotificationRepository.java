package com.company.chatplatform.notificationservice.domain.repository;

import com.company.chatplatform.notificationservice.domain.entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, String> {
    List<NotificationEntity> findByRecipientIdOrderByCreatedAtDesc(String recipientId);
    void deleteByRecipientIdAndChatId(String recipientId, String chatId);
}
