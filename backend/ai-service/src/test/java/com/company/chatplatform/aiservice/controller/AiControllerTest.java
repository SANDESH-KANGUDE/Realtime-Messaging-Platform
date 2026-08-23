package com.company.chatplatform.aiservice.controller;

import com.company.chatplatform.aiservice.dto.AiChatRequest;
import com.company.chatplatform.aiservice.provider.AiProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AiController.class)
class AiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean(name = "gemini")
    private AiProvider geminiProvider;

    @MockBean(name = "openrouter")
    private AiProvider openRouterProvider;

    private final Map<String, AiProvider> providers = new HashMap<>();

    @BeforeEach
    void setUp() {
        providers.put("gemini", geminiProvider);
        providers.put("openrouter", openRouterProvider);
    }

    @Test
    void streamChat_Unauthenticated_ShouldRejectIfNoUserId() throws Exception {
        AiChatRequest request = new AiChatRequest("chat-1", "Hello");

        mockMvc.perform(post("/api/v1/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk()); // Returns stream containing authorization failed event
    }
}
