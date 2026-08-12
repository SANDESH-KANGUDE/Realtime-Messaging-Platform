package com.company.chatplatform.paymentservice.service;

import com.company.chatplatform.paymentservice.domain.entity.PaymentIntentEntity;
import com.company.chatplatform.paymentservice.domain.repository.OutboxEventRepository;
import com.company.chatplatform.paymentservice.domain.repository.PaymentIntentRepository;
import com.company.chatplatform.paymentservice.domain.repository.SubscriptionRepository;
import com.company.chatplatform.paymentservice.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

class PaymentServiceTest {

    private PaymentIntentRepository paymentIntentRepository;
    private SubscriptionRepository subscriptionRepository;
    private OutboxEventRepository outboxEventRepository;
    private ObjectMapper objectMapper;
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentIntentRepository = Mockito.mock(PaymentIntentRepository.class);
        subscriptionRepository = Mockito.mock(SubscriptionRepository.class);
        outboxEventRepository = Mockito.mock(OutboxEventRepository.class);
        objectMapper = new ObjectMapper();

        paymentService = new PaymentService(paymentIntentRepository, subscriptionRepository, outboxEventRepository, objectMapper);
    }

    @Test
    void createCheckoutSession_Success() {
        CreateCheckoutRequest req = new CreateCheckoutRequest("pro", new BigDecimal("9.99"), "USD");

        PaymentIntentDto dto = paymentService.createCheckoutSession("user-1", req, null);

        assertNotNull(dto);
        assertEquals("user-1", dto.getUserId());
        assertEquals("pro", dto.getPlanName());
        assertEquals("PENDING", dto.getStatus());
        Mockito.verify(paymentIntentRepository, Mockito.times(1)).save(any(PaymentIntentEntity.class));
    }

    @Test
    void handleWebhook_Success() {
        PaymentIntentEntity intent = new PaymentIntentEntity("intent-1", "user-1", "pro", new BigDecimal("9.99"), "USD");
        Mockito.when(paymentIntentRepository.findById("intent-1")).thenReturn(Optional.of(intent));

        paymentService.handleWebhook(new WebhookPayload("intent-1", "payment_intent.succeeded"));

        assertEquals("COMPLETED", intent.getStatus());
        Mockito.verify(subscriptionRepository, Mockito.times(1)).save(any());
        Mockito.verify(outboxEventRepository, Mockito.times(2)).save(any());
    }
}
