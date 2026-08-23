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
            EventTopics.FRIEND_REQUEST_SENT,
            "message.read.v1"
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
            } else if ("message.read.v1".equals(topic)) {
                String chatId = (String) map.get("chatId");
                String userId = (String) map.get("userId");
                if (chatId != null && userId != null) {
                    notificationService.deleteChatNotifications(userId, chatId);
                    log.info("Cleared MESSAGE notifications for recipient {} in chat {}", userId, chatId);
                }
            } else if (EventTopics.MESSAGE_SENT.equals(topic)) {
                String chatId = (String) map.get("chatId");
                String senderId = (String) map.get("senderId");
                String content = (String) map.get("content");
                if (chatId != null) {
                    try {
                        String senderName = "Someone";
                        if (senderId != null) {
                            try {
                                String userUrl = "http://localhost:8082/api/v1/users/" + senderId;
                                Map<?, ?> userRes = restTemplate.getForObject(userUrl, Map.class);
                                if (userRes != null && userRes.get("data") != null) {
                                    Map<?, ?> dataMap = (Map<?, ?>) userRes.get("data");
                                    String dispName = (String) dataMap.get("displayName");
                                    String uName = (String) dataMap.get("username");
                                    senderName = (dispName != null && !dispName.isBlank()) ? dispName : uName;
                                }
                            } catch (Exception ue) {
                                log.error("Failed to fetch sender profile from user-service", ue);
                            }
                        }

                        String url = "http://localhost:8083/internal/v1/chats/" + chatId + "/active-member-ids";
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
                                            senderName,
                                            content != null ? content : "sent a message",
                                            "MESSAGE",
                                            chatId
                                    );
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.error("Failed to retrieve active chat members for message notification", e);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to process notification event", e);
        }
    }
}
