package com.company.chatplatform.paymentservice.service;

import com.company.chatplatform.common.core.exception.ResourceNotFoundException;
import com.company.chatplatform.common.core.util.UUIDv7Utils;
import com.company.chatplatform.common.kafka.topics.EventTopics;
import com.company.chatplatform.paymentservice.domain.entity.OutboxEventEntity;
import com.company.chatplatform.paymentservice.domain.entity.PaymentIntentEntity;
import com.company.chatplatform.paymentservice.domain.entity.SubscriptionEntity;
import com.company.chatplatform.paymentservice.domain.repository.OutboxEventRepository;
import com.company.chatplatform.paymentservice.domain.repository.PaymentIntentRepository;
import com.company.chatplatform.paymentservice.domain.repository.SubscriptionRepository;
import com.company.chatplatform.paymentservice.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@Service
public class PaymentService {

    private final PaymentIntentRepository paymentIntentRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    private final java.util.concurrent.ConcurrentMap<String, PaymentIntentDto> idempotencyCache = new java.util.concurrent.ConcurrentHashMap<>();

    public PaymentService(PaymentIntentRepository paymentIntentRepository, SubscriptionRepository subscriptionRepository, OutboxEventRepository outboxEventRepository, ObjectMapper objectMapper) {
        this.paymentIntentRepository = paymentIntentRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public PaymentIntentDto createCheckoutSession(String userId, CreateCheckoutRequest request, String idempotencyKey) {
        if (idempotencyKey != null && idempotencyCache.containsKey(idempotencyKey)) {
            return idempotencyCache.get(idempotencyKey);
        }

        String id = UUIDv7Utils.generateString();
        PaymentIntentEntity intent = new PaymentIntentEntity(
                id,
                userId,
                request.getPlanName(),
                request.getAmount(),
                request.getCurrency()
        );
        paymentIntentRepository.save(intent);

        PaymentIntentDto dto = toDto(intent);
        if (idempotencyKey != null) {
            idempotencyCache.put(idempotencyKey, dto);
        }
        return dto;
    }

    @Transactional
    public void handleWebhook(WebhookPayload payload) {
        PaymentIntentEntity intent = paymentIntentRepository.findById(payload.getPaymentIntentId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment intent not found", "PAYMENT_NOT_FOUND"));

        if ("payment_intent.succeeded".equalsIgnoreCase(payload.getEvent())) {
            intent.setStatus("COMPLETED");
            intent.setUpdatedAt(Instant.now());
            paymentIntentRepository.save(intent);

            // Update/Create Subscription
            Instant expiresAt = Instant.now().plus(30, ChronoUnit.DAYS);
            SubscriptionEntity sub = subscriptionRepository.findByUserId(intent.getUserId())
                    .orElseGet(() -> new SubscriptionEntity(UUIDv7Utils.generateString(), intent.getUserId(), intent.getPlanName(), expiresAt));
            sub.setPlanName(intent.getPlanName());
            sub.setStatus("ACTIVE");
            sub.setExpiresAt(expiresAt);
            sub.setUpdatedAt(Instant.now());
            subscriptionRepository.save(sub);

            // Save Outbox Events
            saveOutboxEvent(EventTopics.PAYMENT_COMPLETED, intent.getId(), Map.of(
                    "paymentIntentId", intent.getId(),
                    "userId", intent.getUserId(),
                    "amount", intent.getAmount(),
                    "planName", intent.getPlanName()
            ));

            saveOutboxEvent(EventTopics.SUBSCRIPTION_ACTIVATED, sub.getId(), Map.of(
                    "subscriptionId", sub.getId(),
                    "userId", sub.getUserId(),
                    "planName", sub.getPlanName(),
                    "expiresAt", sub.getExpiresAt().toString()
            ));
        }
    }

    public SubscriptionDto getSubscription(String userId) {
        SubscriptionEntity sub = subscriptionRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Active subscription not found", "SUBSCRIPTION_NOT_FOUND"));
        return new SubscriptionDto(
                sub.getId(),
                sub.getUserId(),
                sub.getPlanName(),
                sub.getStatus(),
                sub.getExpiresAt().toString(),
                sub.getCreatedAt().toString()
        );
    }

    private PaymentIntentDto toDto(PaymentIntentEntity intent) {
        return new PaymentIntentDto(
                intent.getId(),
                intent.getUserId(),
                intent.getPlanName(),
                intent.getAmount(),
                intent.getCurrency(),
                intent.getStatus(),
                intent.getCreatedAt().toString()
        );
    }

    private void saveOutboxEvent(String eventType, String aggregateId, Map<String, Object> payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            OutboxEventEntity event = new OutboxEventEntity(
                    UUIDv7Utils.generateString(),
                    "PAYMENT",
                    aggregateId,
                    eventType,
                    json
            );
            outboxEventRepository.save(event);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize outbox payload", e);
        }
    }
}
