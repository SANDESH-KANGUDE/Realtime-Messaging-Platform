# 19_payment-service.md

Version: 1.0
Status: FINAL

---

# Purpose

Manage subscriptions and payments.

---

# Responsibilities

- Subscription Plans
- Payment Processing
- Renewals
- Billing History

---

# Database

Tables

- subscriptions
- payments

---

# APIs

GET /plans

POST /subscriptions

GET /subscriptions/me

POST /payments/webhook

---

# Kafka Events

Publish

- payment.completed
- subscription.activated
- subscription.expired

Consume

- user.updated

---

# Business Rules

- Webhooks are idempotent
- One active subscription per user
- Payment history immutable

---

# Security

- Webhook signature verification
- PCI compliance (payment provider handles card data)
- Sensitive payment data never stored

---

# Testing

- Subscription lifecycle
- Webhook processing
- Renewal
- Failure handling

---

Status: FINAL