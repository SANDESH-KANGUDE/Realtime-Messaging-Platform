package com.company.chatplatform.notificationservice.listener;

import com.company.chatplatform.common.kafka.topics.EventTopics;
import com.company.chatplatform.notificationservice.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

@Component
public class NotificationKafkaListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationKafkaListener.class);
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    public NotificationKafkaListener(NotificationService notificationService, ObjectMapper objectMapper) {
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = {
            EventTopics.MESSAGE_SENT,
            EventTopics.FRIEND_REQUEST_SENT
    }, groupId = "notification-service-group")
    public void onEvent(
            String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic
    ) {
        try {
            log.info("Notification Service received event on topic {}: {}", topic, message);
            Map<?, ?> map = objectMapper.readValue(message, Map.class);

            if (EventTopics.FRIEND_REQUEST_SENT.equals(topic)) {
                String addresseeId = (String) map.get("addresseeId");
                if (addresseeId != null) {
                    notificationService.createNotification(addresseeId, "New Friend Request", "You received a new friend request!", "FRIEND_REQUEST");
                }
            } else if (EventTopics.MESSAGE_SENT.equals(topic)) {
                String chatId = (String) map.get("chatId");
                String senderId = (String) map.get("senderId");
                String content = (String) map.get("content");
                if (chatId != null) {
                    try {
                        String url = "http://localhost:8083/internal/v1/chats/" + chatId + "/member-ids";
                        HttpHeaders headers = new HttpHeaders();
                        headers.set("X-Internal-Token", "secret-internal-service-token");
                        HttpEntity<Void> entity = new HttpEntity<>(headers);
                        ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, entity, List.class);
                        List<String> memberIds = response.getBody();
                        if (memberIds != null) {
                            for (String memberId : memberIds) {
                                if (senderId == null || !memberId.equals(senderId)) {
                                    notificationService.createNotification(
                                            memberId,
                                            "New Message",
                                            content != null ? content : "You received a new message",
                                            "MESSAGE"
                                    );
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.error("Failed to retrieve chat members for message notification", e);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to process notification event", e);
        }
    }
}
