package com.company.chatplatform.realtimeservice.listener;

import com.company.chatplatform.common.kafka.topics.EventTopics;
import com.company.chatplatform.realtimeservice.socket.SocketIOServerManager;
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
public class RealtimeKafkaListener {

    private static final Logger log = LoggerFactory.getLogger(RealtimeKafkaListener.class);
    private final SocketIOServerManager socketManager;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    public RealtimeKafkaListener(SocketIOServerManager socketManager, ObjectMapper objectMapper) {
        this.socketManager = socketManager;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = {EventTopics.MESSAGE_SENT, EventTopics.MESSAGE_EDITED, EventTopics.MESSAGE_DELETED, EventTopics.MESSAGE_READ, EventTopics.FRIEND_REQUEST_SENT, "friend.request.accepted.v1"}, groupId = "realtime-service-group")
    public void handleMessageEvent(
            String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic
    ) {
        try {
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            
            if (EventTopics.FRIEND_REQUEST_SENT.equals(topic)) {
                String addresseeId = (String) event.get("addresseeId");
                if (addresseeId != null) {
                    socketManager.broadcastToRoom(addresseeId, "friend_request_received", event);
                    log.info("Broadcasted friend_request_received event to user room '{}'", addresseeId);
                }
                return;
            }

            if ("friend.request.accepted.v1".equals(topic)) {
                String requesterId = (String) event.get("requesterId");
                String addresseeId = (String) event.get("addresseeId");
                if (requesterId != null) {
                    socketManager.broadcastToRoom(requesterId, "friend_request_accepted", event);
                }
                if (addresseeId != null) {
                    socketManager.broadcastToRoom(addresseeId, "friend_request_accepted", event);
                }
                log.info("Broadcasted friend_request_accepted event to requester '{}' and addressee '{}'", requesterId, addresseeId);
                return;
            }

            String chatId = (String) event.get("chatId");
            if (chatId != null) {
                String socketEventName = "message_received";
                if (EventTopics.MESSAGE_EDITED.equals(topic)) {
                    socketEventName = "message_edited";
                } else if (EventTopics.MESSAGE_DELETED.equals(topic)) {
                    socketEventName = "message_deleted";
                } else if (EventTopics.MESSAGE_READ.equals(topic) || "message.read.v1".equals(topic)) {
                    socketEventName = "message_read";
                }
                
                // Broadcast to the chatId room
                socketManager.broadcastToRoom(chatId, socketEventName, event);
                
                // Fetch member IDs from chat-service and broadcast to each member's private room (except the sender to avoid duplicates)
                try {
                    String url = "http://localhost:8083/internal/v1/chats/" + chatId + "/member-ids";
                    HttpHeaders headers = new HttpHeaders();
                    headers.set("X-Internal-Token", "secret-internal-service-token");
                    HttpEntity<Void> entity = new HttpEntity<>(headers);
                    ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, entity, List.class);
                    List<String> memberIds = response.getBody();
                    if (memberIds != null) {
                        String senderId = (String) event.get("senderId");
                        for (String memberId : memberIds) {
                            if (senderId == null || !memberId.equals(senderId)) {
                                socketManager.broadcastToRoom(memberId, socketEventName, event);
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("Failed to broadcast message event internally to chat members", e);
                }
                
                log.info("Broadcasted socket event '{}' to room '{}'", socketEventName, chatId);
            } else {
                log.warn("Realtime Kafka event received on topic '{}' without chatId: {}", topic, message);
            }
        } catch (Exception e) {
            log.error("Failed to process realtime Kafka event", e);
        }
    }
}
