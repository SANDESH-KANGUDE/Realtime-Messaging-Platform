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

@Component("openrouter")
public class OpenRouterProvider implements AiProvider {

    private static final Logger log = LoggerFactory.getLogger(OpenRouterProvider.class);

    private final String apiKey;
    private final String defaultModel;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public OpenRouterProvider(
            @Value("${ai.openrouter.api-key:}") String apiKey,
            @Value("${ai.openrouter.model:google/gemini-2.5-flash:free}") String defaultModel,
            ObjectMapper objectMapper
    ) {
        this.apiKey = apiKey;
        this.defaultModel = defaultModel;
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
            errorConsumer.accept(new IllegalStateException("OpenRouter API key is not configured."));
            return;
        }

        try {
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", systemInstruction));
            for (ChatMessage msg : history) {
                messages.add(Map.of("role", msg.role().toLowerCase(), "content", msg.content()));
            }

            Map<String, Object> requestMap = Map.of(
                    "model", defaultModel,
                    "messages", messages,
                    "stream", true
            );

            String requestBody = objectMapper.writeValueAsString(requestMap);
            URI uri = URI.create("https://openrouter.ai/api/v1/chat/completions");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
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
                                log.error("OpenRouter API error (Status: {}): {}", statusCode, errBody);
                                errorConsumer.accept(new RuntimeException("OpenRouter API returned error status: " + statusCode));
                            } catch (Exception ex) {
                                errorConsumer.accept(new RuntimeException("OpenRouter API error status: " + statusCode));
                            }
                            return;
                        }

                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body(), StandardCharsets.UTF_8))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                String trimmed = line.trim();
                                if (trimmed.startsWith("data: ")) {
                                    String dataVal = trimmed.substring(6).trim();
                                    if ("[DONE]".equals(dataVal)) {
                                        break;
                                    }
                                    try {
                                        JsonNode root = objectMapper.readTree(dataVal);
                                        JsonNode choices = root.path("choices");
                                        if (choices.isArray() && choices.size() > 0) {
                                            JsonNode delta = choices.get(0).path("delta");
                                            JsonNode content = delta.path("content");
                                            if (!content.isMissingNode() && !content.isNull()) {
                                                chunkConsumer.accept(content.asText());
                                            }
                                        }
                                    } catch (Exception ex) {
                                        // Ignore malformed json in sse
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
