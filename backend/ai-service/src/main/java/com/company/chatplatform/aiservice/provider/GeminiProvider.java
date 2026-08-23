package com.company.chatplatform.aiservice.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Component("gemini")
public class GeminiProvider implements AiProvider {

    private static final Logger log = LoggerFactory.getLogger(GeminiProvider.class);

    private final String apiKey;
    private final String model;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public GeminiProvider(
            @Value("${ai.gemini.api-key:}") String apiKey,
            @Value("${ai.model:gemini-2.5-flash}") String model,
            ObjectMapper objectMapper
    ) {
        this.apiKey = apiKey;
        this.model = model;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder().build();
    }

    @Override
    public void streamChat(
            List<ChatMessage> history,
            String systemInstruction,
            Consumer<String> chunkConsumer,
            Consumer<Throwable> errorConsumer,
            Runnable onComplete
    ) {
        if (apiKey == null || apiKey.isBlank()) {
            errorConsumer.accept(new IllegalStateException("Gemini API key is not configured."));
            return;
        }

        try {
            // Map messages to Gemini's format: user -> user, assistant -> model
            // Gemini API requires alternating roles starting with "user".
            List<Map<String, Object>> contents = new ArrayList<>();
            String lastRole = null;
            StringBuilder joinedText = new StringBuilder();

            for (ChatMessage msg : history) {
                String geminiRole = "assistant".equalsIgnoreCase(msg.role()) ? "model" : "user";
                
                // Conversational history must start with "user"
                if (lastRole == null && "model".equals(geminiRole)) {
                    continue;
                }

                if (geminiRole.equals(lastRole)) {
                    // Merge consecutive duplicate roles
                    joinedText.append("\n").append(msg.content());
                } else {
                    if (lastRole != null) {
                        contents.add(Map.of(
                                "role", lastRole,
                                "parts", List.of(Map.of("text", joinedText.toString()))
                        ));
                    }
                    lastRole = geminiRole;
                    joinedText = new StringBuilder(msg.content());
                }
            }

            if (lastRole != null) {
                contents.add(Map.of(
                        "role", lastRole,
                        "parts", List.of(Map.of("text", joinedText.toString()))
                ));
            }

            if (contents.isEmpty()) {
                // Fallback to avoid empty contents error
                contents.add(Map.of(
                        "role", "user",
                        "parts", List.of(Map.of("text", "Hello"))
                ));
            }

            Map<String, Object> requestMap = Map.of(
                    "contents", contents,
                    "systemInstruction", Map.of(
                            "parts", List.of(Map.of("text", systemInstruction))
                    )
            );

            String requestBody = objectMapper.writeValueAsString(requestMap);
            String url = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":streamGenerateContent?key=" + apiKey;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream())
                    .thenAccept(response -> {
                        int statusCode = response.statusCode();
                        if (statusCode == 429) {
                            errorConsumer.accept(new RuntimeException("AI_PROVIDER_RATE_LIMITED"));
                            return;
                        }
                        if (statusCode >= 400) {
                            try {
                                String errBody = new String(response.body().readAllBytes(), StandardCharsets.UTF_8);
                                log.error("Gemini API error (Status: {}): {}", statusCode, errBody);
                                errorConsumer.accept(new RuntimeException("Gemini API returned error status: " + statusCode));
                            } catch (Exception ex) {
                                errorConsumer.accept(new RuntimeException("Gemini API error status: " + statusCode));
                            }
                            return;
                        }

                        try (com.fasterxml.jackson.core.JsonParser parser = objectMapper.getFactory().createParser(response.body())) {
                            if (parser.nextToken() == com.fasterxml.jackson.core.JsonToken.START_ARRAY) {
                                while (parser.nextToken() != com.fasterxml.jackson.core.JsonToken.END_ARRAY && parser.getCurrentToken() != null) {
                                    JsonNode root = parser.readValueAs(JsonNode.class);
                                    JsonNode candidates = root.path("candidates");
                                    if (candidates.isArray() && candidates.size() > 0) {
                                        JsonNode content = candidates.get(0).path("content");
                                        JsonNode parts = content.path("parts");
                                        if (parts.isArray() && parts.size() > 0) {
                                            String text = parts.get(0).path("text").asText();
                                            if (text != null && !text.isEmpty()) {
                                                chunkConsumer.accept(text);
                                            }
                                        }
                                    }
                                }
                            }
                            onComplete.run();
                        } catch (Exception e) {
                            errorConsumer.accept(e);
                        }
                    })
                    .exceptionally(throwable -> {
                        errorConsumer.accept(throwable);
                        return null;
                    });

        } catch (Exception e) {
            errorConsumer.accept(e);
        }
    }
}
