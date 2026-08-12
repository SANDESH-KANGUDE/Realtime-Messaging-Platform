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

import java.util.Map;

@Component
public class RealtimeKafkaListener {

    private static final Logger log = LoggerFactory.getLogger(RealtimeKafkaListener.class);
    private final SocketIOServerManager socketManager;
    private final ObjectMapper objectMapper;

    public RealtimeKafkaListener(SocketIOServerManager socketManager, ObjectMapper objectMapper) {
        this.socketManager = socketManager;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = {EventTopics.MESSAGE_SENT, EventTopics.MESSAGE_EDITED, EventTopics.MESSAGE_DELETED}, groupId = "realtime-service-group")
    public void handleMessageEvent(
            String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic
    ) {
        try {
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            String chatId = (String) event.get("chatId");
            
            if (chatId != null) {
                String socketEventName = "message_received";
                if (EventTopics.MESSAGE_EDITED.equals(topic)) {
                    socketEventName = "message_edited";
                } else if (EventTopics.MESSAGE_DELETED.equals(topic)) {
                    socketEventName = "message_deleted";
                }
                
                socketManager.broadcastToRoom(chatId, socketEventName, event);
                log.info("Broadcasted socket event '{}' to room '{}'", socketEventName, chatId);
            } else {
                log.warn("Realtime Kafka event received on topic '{}' without chatId: {}", topic, message);
            }
        } catch (Exception e) {
            log.error("Failed to process realtime Kafka event", e);
        }
    }
}
