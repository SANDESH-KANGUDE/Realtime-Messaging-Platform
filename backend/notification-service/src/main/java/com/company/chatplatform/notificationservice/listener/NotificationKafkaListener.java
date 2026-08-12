package com.company.chatplatform.notificationservice.listener;

import com.company.chatplatform.common.kafka.topics.EventTopics;
import com.company.chatplatform.notificationservice.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConditionalOnProperty(name = "spring.kafka.bootstrap-servers")
public class NotificationKafkaListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationKafkaListener.class);

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    public NotificationKafkaListener(NotificationService notificationService, ObjectMapper objectMapper) {
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = {
            EventTopics.MESSAGE_SENT,
            EventTopics.FRIEND_REQUEST_SENT,
            EventTopics.PAYMENT_COMPLETED,
            EventTopics.SUBSCRIPTION_ACTIVATED
    }, groupId = "notification-service-group")
    public void onEvent(String message) {
        try {
            log.info("Notification Service received event: {}", message);
            Map<?, ?> map = objectMapper.readValue(message, Map.class);
            String addresseeId = (String) map.get("addresseeId");
            String userId = (String) map.get("userId");
            String planName = (String) map.get("planName");

            if (addresseeId != null) {
                notificationService.createNotification(addresseeId, "New Friend Request", "You received a new friend request!", "FRIEND_REQUEST");
            } else if (userId != null && planName != null) {
                notificationService.createNotification(userId, "Subscription Activated", "Your " + planName + " subscription is now active!", "PAYMENT");
            }
        } catch (Exception e) {
            log.error("Failed to process notification event", e);
        }
    }
}
