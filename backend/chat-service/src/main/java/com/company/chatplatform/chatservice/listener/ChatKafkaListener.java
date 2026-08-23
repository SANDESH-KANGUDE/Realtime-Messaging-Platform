package com.company.chatplatform.chatservice.listener;

import com.company.chatplatform.chatservice.domain.entity.ChatEntity;
import com.company.chatplatform.chatservice.domain.repository.ChatRepository;
import com.company.chatplatform.common.kafka.topics.EventTopics;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;

@Component
public class ChatKafkaListener {

    private static final Logger log = LoggerFactory.getLogger(ChatKafkaListener.class);
    private final ChatRepository chatRepository;
    private final ObjectMapper objectMapper;

    public ChatKafkaListener(ChatRepository chatRepository, ObjectMapper objectMapper) {
        this.chatRepository = chatRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = {EventTopics.MESSAGE_SENT}, groupId = "chat-service-group")
    @Transactional
    public void onMessageSent(String message) {
        try {
            Map<?, ?> map = objectMapper.readValue(message, Map.class);
            String chatId = (String) map.get("chatId");
            if (chatId != null) {
                chatRepository.findById(chatId).ifPresent(chat -> {
                    chat.setUpdatedAt(Instant.now());
                    chatRepository.save(chat);
                    log.info("Updated chat {} updatedAt timestamp to now due to MESSAGE_SENT event", chatId);
                });
            }
        } catch (Exception e) {
            log.error("Failed to process message sent event in chat-service", e);
        }
    }
}
