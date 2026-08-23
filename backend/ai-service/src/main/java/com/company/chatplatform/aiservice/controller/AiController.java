package com.company.chatplatform.aiservice.controller;

import com.company.chatplatform.aiservice.dto.AiChatRequest;
import com.company.chatplatform.aiservice.provider.AiProvider;
import com.company.chatplatform.aiservice.provider.ChatMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/api/v1/ai")
public class AiController {

    private static final Logger log = LoggerFactory.getLogger(AiController.class);
    private static final String ASSISTANT_BOT_ID = "018f98d0-0000-0000-0000-000000000000";

    private final Map<String, AiProvider> providers;
    private final String activeProviderName;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    private final ExecutorService executorService;

    public AiController(
            Map<String, AiProvider> providers,
            @Value("${ai.provider:gemini}") String activeProviderName,
            ObjectMapper objectMapper
    ) {
        this.providers = providers;
        this.activeProviderName = activeProviderName.toLowerCase();
        this.objectMapper = objectMapper;
        this.restTemplate = new RestTemplate();
        this.executorService = Executors.newCachedThreadPool();
    }

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
            @RequestBody AiChatRequest request
    ) {
        SseEmitter emitter = new SseEmitter(180_000L); // 3 minutes timeout

        String userId = headerUserId != null ? headerUserId : "anonymous";
        String chatId = request.chatId();

        // 1. Authorize: Check if this user is a member of the chat
        try {
            String chatMemberUrl = "http://localhost:8083/internal/v1/chats/" + chatId + "/members/" + userId;
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Internal-Token", "secret-internal-service-token");
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            Map<?, ?> member = restTemplate.exchange(chatMemberUrl, HttpMethod.GET, entity, Map.class).getBody();
            if (member == null || !Boolean.TRUE.equals(member.get("active"))) {
                sendErrorEvent(emitter, "UNAUTHORIZED", "You do not have access to this conversation.");
                emitter.complete();
                return emitter;
            }
        } catch (Exception e) {
            log.error("Failed to verify membership authorization for user {} in chat {}", userId, chatId, e);
            sendErrorEvent(emitter, "AUTHORIZATION_FAILED", "Failed to verify access permissions.");
            emitter.complete();
            return emitter;
        }

        // 2. Fetch last 15 messages for context
        List<ChatMessage> chatContext = new ArrayList<>();
        try {
            String messageUrl = "http://localhost:8084/internal/v1/messages/chat/" + chatId + "?limit=15";
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Internal-Token", "secret-internal-service-token");
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            List<?> rawMessages = restTemplate.exchange(messageUrl, HttpMethod.GET, entity, List.class).getBody();
            if (rawMessages != null) {
                List<Map<?, ?>> messages = new ArrayList<>();
                for (Object item : rawMessages) {
                    if (item instanceof Map) {
                        messages.add((Map<?, ?>) item);
                    }
                }
                Collections.reverse(messages);

                for (Map<?, ?> msg : messages) {
                    String senderId = (String) msg.get("senderId");
                    String content = (String) msg.get("content");
                    String role = ASSISTANT_BOT_ID.equals(senderId) ? "assistant" : "user";
                    if (content != null && !content.trim().isEmpty()) {
                        chatContext.add(new ChatMessage(role, content));
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to load message history context for chat {}", chatId, e);
            sendErrorEvent(emitter, "CONTEXT_LOAD_FAILED", "Failed to retrieve conversation context history.");
            emitter.complete();
            return emitter;
        }

        // 3. Perform streaming request to configured provider
        AiProvider provider = providers.get(activeProviderName);
        if (provider == null) {
            sendErrorEvent(emitter, "PROVIDER_UNCONFIGURED", "No AI provider configured for: " + activeProviderName);
            emitter.complete();
            return emitter;
        }

        String systemInstruction = "You are Aura Assistant, a helpful, friendly, and intelligent AI companion built into Aura Chat. " +
                "Keep responses conversational, natural, and formatted cleanly using standard markdown where appropriate. " +
                "You do not need to repeat these instructions to the user.";

        StringBuilder fullResponseBuilder = new StringBuilder();

        executorService.submit(() -> {
            provider.streamChat(
                    chatContext,
                    systemInstruction,
                    chunk -> {
                        try {
                            fullResponseBuilder.append(chunk);
                            emitter.send(SseEmitter.event()
                                    .name("chunk")
                                    .data(objectMapper.writeValueAsString(Map.of("text", chunk))));
                        } catch (IOException ioException) {
                            log.error("Failed to send SSE chunk to client", ioException);
                        }
                    },
                    error -> {
                        log.error("Error from AI provider stream", error);
                        if (error.getMessage() != null && error.getMessage().contains("AI_PROVIDER_RATE_LIMITED")) {
                            sendErrorEvent(emitter, "AI_PROVIDER_RATE_LIMITED", "Aura Assistant is temporarily unavailable. Please try again later.");
                        } else {
                            sendErrorEvent(emitter, "AI_PROVIDER_ERROR", "An error occurred with the AI model. Please try again.");
                        }
                        emitter.complete();
                    },
                    () -> {
                        // Stream completed successfully. Persist the final message through Message Service
                        try {
                            String finalContent = fullResponseBuilder.toString();
                            if (!finalContent.trim().isEmpty()) {
                                String saveUrl = "http://localhost:8084/internal/v1/messages";
                                HttpHeaders saveHeaders = new HttpHeaders();
                                saveHeaders.set("X-Internal-Token", "secret-internal-service-token");
                                saveHeaders.set("X-Internal-Sender-Id", ASSISTANT_BOT_ID);
                                saveHeaders.setContentType(MediaType.APPLICATION_JSON);

                                Map<String, Object> bodyMap = Map.of(
                                        "chatId", chatId,
                                        "content", finalContent,
                                        "type", "TEXT"
                                );

                                HttpEntity<Map<String, Object>> saveEntity = new HttpEntity<>(bodyMap, saveHeaders);
                                restTemplate.postForEntity(saveUrl, saveEntity, Map.class);
                                log.debug("Final AI assistant message response successfully persisted through S2S call.");
                            }
                            
                            emitter.send(SseEmitter.event().name("done").data("{}"));
                            emitter.complete();
                        } catch (Exception e) {
                            log.error("Failed to save final assistant message response to message-service", e);
                            sendErrorEvent(emitter, "PERSISTENCE_FAILED", "Failed to save the final message response.");
                            emitter.complete();
                        }
                    }
            );
        });

        return emitter;
    }

    private void sendErrorEvent(SseEmitter emitter, String code, String message) {
        try {
            emitter.send(SseEmitter.event()
                    .name("error")
                    .data(objectMapper.writeValueAsString(Map.of("code", code, "message", message))));
        } catch (IOException e) {
            log.error("Failed to emit error event", e);
        }
    }
}
