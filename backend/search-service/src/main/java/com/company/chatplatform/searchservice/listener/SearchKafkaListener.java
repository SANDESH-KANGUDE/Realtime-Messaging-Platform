package com.company.chatplatform.searchservice.listener;

import com.company.chatplatform.common.kafka.topics.EventTopics;
import com.company.chatplatform.searchservice.service.SearchService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SearchKafkaListener {

    private static final Logger log = LoggerFactory.getLogger(SearchKafkaListener.class);

    private final SearchService searchService;
    private final ObjectMapper objectMapper;

    public SearchKafkaListener(SearchService searchService, ObjectMapper objectMapper) {
        this.searchService = searchService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = {EventTopics.MESSAGE_SENT, EventTopics.USER_PROFILE_UPDATED, EventTopics.CHAT_CREATED}, groupId = "search-service-group")
    public void onDomainEvent(String message) {
        try {
            log.info("Search Service received domain event: {}", message);
            Map<?, ?> map = objectMapper.readValue(message, Map.class);
            String messageId = (String) map.get("messageId");
            String userId = (String) map.get("userId");
            String chatId = (String) map.get("chatId");
            String content = (String) map.get("content");
            String displayName = (String) map.get("displayName");
            String title = (String) map.get("title");

            if (messageId != null && content != null) {
                searchService.index(messageId, "MESSAGE", "Message in Chat " + chatId, content, chatId);
            } else if (userId != null && displayName != null) {
                searchService.index(userId, "USER", displayName, (String) map.get("username"), (String) map.get("email"));
            } else if (chatId != null && title != null) {
                searchService.index(chatId, "CHAT", title, "Group Chat", "");
            }
        } catch (Exception e) {
            log.error("Failed to index event into search service", e);
        }
    }
}
