package com.company.chatplatform.userservice.listener;

import com.company.chatplatform.common.kafka.topics.EventTopics;
import com.company.chatplatform.userservice.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConditionalOnProperty(name = "spring.kafka.bootstrap-servers")
public class AuthUserRegisteredListener {

    private static final Logger log = LoggerFactory.getLogger(AuthUserRegisteredListener.class);

    private final UserService userService;
    private final ObjectMapper objectMapper;

    public AuthUserRegisteredListener(UserService userService, ObjectMapper objectMapper) {
        this.userService = userService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = EventTopics.AUTH_USER_REGISTERED, groupId = "user-service-group")
    public void onUserRegistered(String message) {
        try {
            log.info("Received AuthUserRegistered event: {}", message);
            Map<?, ?> map = objectMapper.readValue(message, Map.class);
            String userId = (String) map.get("userId");
            String email = (String) map.get("email");
            String username = (String) map.get("username");
            String displayName = (String) map.get("displayName");
            String phoneNumber = (String) map.get("phoneNumber");

            userService.createProfileFromRegistration(userId, email, username, displayName, phoneNumber);
            log.info("Successfully provisioned User Profile for userId: {}", userId);
        } catch (Exception e) {
            log.error("Failed to process AuthUserRegistered event", e);
        }
    }
}
