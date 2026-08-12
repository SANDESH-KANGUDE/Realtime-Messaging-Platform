package com.company.chatplatform.paymentservice.controller;

import com.company.chatplatform.common.core.dto.ApiResponse;
import com.company.chatplatform.common.security.context.UserContextHolder;
import com.company.chatplatform.paymentservice.dto.*;
import com.company.chatplatform.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/plans")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getPlans() {
        List<Map<String, Object>> plans = List.of(
                Map.of("id", "free", "name", "Free Plan", "price", 0.00, "features", List.of("Standard messaging", "Direct chats")),
                Map.of("id", "pro", "name", "Pro Premium", "price", 9.99, "features", List.of("Unlimited group chats", "HD Media sharing", "Search history", "Priority support")),
                Map.of("id", "enterprise", "name", "Enterprise", "price", 49.99, "features", List.of("Dedicated channels", "Admin dashboard", "Unlimited cloud storage"))
        );
        return ResponseEntity.ok(ApiResponse.success(plans));
    }

    @PostMapping(value = {"", "/checkout"})
    public ResponseEntity<ApiResponse<PaymentIntentDto>> createCheckoutSession(
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody CreateCheckoutRequest request
    ) {
        String userId = headerUserId != null ? headerUserId : UserContextHolder.getUserId();
        
        if (request.getPlanId() != null) {
            if ("PREMIUM_MONTHLY".equalsIgnoreCase(request.getPlanId()) || "pro".equalsIgnoreCase(request.getPlanId())) {
                request.setPlanName("Pro Premium");
                request.setAmount(new java.math.BigDecimal("9.99"));
            } else if ("enterprise".equalsIgnoreCase(request.getPlanId())) {
                request.setPlanName("Enterprise");
                request.setAmount(new java.math.BigDecimal("49.99"));
            } else {
                request.setPlanName(request.getPlanId());
                request.setAmount(new java.math.BigDecimal("0.00"));
            }
        }
        
        if (request.getPlanName() == null) {
            request.setPlanName("Pro Premium");
        }
        if (request.getAmount() == null) {
            request.setAmount(new java.math.BigDecimal("9.99"));
        }
        
        PaymentIntentDto intent = paymentService.createCheckoutSession(userId, request, idempotencyKey);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(intent, "Checkout session created"));
    }

    @PostMapping(value = {"/webhook", "/webhooks/payment-provider"})
    public ResponseEntity<ApiResponse<Void>> handleWebhook(@RequestBody WebhookPayload payload) {
        paymentService.handleWebhook(payload);
        return ResponseEntity.ok(ApiResponse.success(null, "Webhook processed"));
    }

    @GetMapping("/subscription")
    public ResponseEntity<ApiResponse<SubscriptionDto>> getSubscription(
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId
    ) {
        String userId = headerUserId != null ? headerUserId : UserContextHolder.getUserId();
        SubscriptionDto subscription = paymentService.getSubscription(userId);
        return ResponseEntity.ok(ApiResponse.success(subscription));
    }
}
