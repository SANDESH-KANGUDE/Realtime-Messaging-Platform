package com.company.chatplatform.messageservice.service;

import com.company.chatplatform.common.core.exception.ResourceNotFoundException;
import com.company.chatplatform.common.core.exception.ForbiddenException;
import com.company.chatplatform.common.core.util.UUIDv7Utils;
import com.company.chatplatform.common.kafka.topics.EventTopics;
import com.company.chatplatform.messageservice.domain.document.MessageDocument;
import com.company.chatplatform.messageservice.domain.document.OutboxMessageDocument;
import com.company.chatplatform.messageservice.domain.document.ReactionDocument;
import com.company.chatplatform.messageservice.domain.document.ReadReceiptDocument;
import com.company.chatplatform.messageservice.domain.document.DeliveryReceiptDocument;
import com.company.chatplatform.messageservice.domain.repository.MessageRepository;
import com.company.chatplatform.messageservice.domain.repository.OutboxMessageRepository;
import com.company.chatplatform.messageservice.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final OutboxMessageRepository outboxMessageRepository;
    private final ObjectMapper objectMapper;

    public MessageService(MessageRepository messageRepository, OutboxMessageRepository outboxMessageRepository, ObjectMapper objectMapper) {
        this.messageRepository = messageRepository;
        this.outboxMessageRepository = outboxMessageRepository;
        this.objectMapper = objectMapper;
    }

    private final org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();

    private void checkBlockStatus(String senderId, String chatId) {
        try {
            // Check active member status in chat
            String chatMemberUrl = "http://localhost:8083/internal/v1/chats/" + chatId + "/members/" + senderId;
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("X-Internal-Token", "secret-internal-service-token");
            org.springframework.http.HttpEntity<Void> entity = new org.springframework.http.HttpEntity<>(headers);
            org.springframework.http.ResponseEntity<Map> memberResponse = restTemplate.exchange(
                    chatMemberUrl,
                    org.springframework.http.HttpMethod.GET,
                    entity,
                    Map.class
            );
            Map<?, ?> member = memberResponse.getBody();
            if (member != null) {
                Boolean active = (Boolean) member.get("active");
                if (Boolean.FALSE.equals(active)) {
                    throw new ForbiddenException("Cannot send message. You are no longer a member of this group.", "NOT_A_MEMBER");
                }
            }
        } catch (ForbiddenException fe) {
            throw fe;
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(MessageService.class).warn("Failed to verify active membership for user {} in chat {}", senderId, chatId, e);
        }

        try {
            // 1. Get chat member IDs from chat-service
            String chatUrl = "http://localhost:8083/internal/v1/chats/" + chatId + "/member-ids";
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("X-Internal-Token", "secret-internal-service-token");
            org.springframework.http.HttpEntity<Void> entity = new org.springframework.http.HttpEntity<>(headers);
            org.springframework.http.ResponseEntity<List> response = restTemplate.exchange(
                    chatUrl,
                    org.springframework.http.HttpMethod.GET,
                    entity,
                    List.class
            );
            List<String> memberIds = response.getBody();
            if (memberIds != null && memberIds.size() == 2) {
                // It is a direct chat. Get the other member ID
                String otherMemberId = memberIds.get(0).equals(senderId) ? memberIds.get(1) : memberIds.get(0);
                
                // 2. Check block status in user-service
                String userUrl = "http://localhost:8082/api/v1/users/internal/is-blocked?user1=" + senderId + "&user2=" + otherMemberId;
                Boolean isBlocked = restTemplate.getForObject(userUrl, Boolean.class);
                if (Boolean.TRUE.equals(isBlocked)) {
                    throw new ForbiddenException("Cannot send message. One of the users is blocked.", "USER_BLOCKED");
                }
            }
        } catch (ForbiddenException fe) {
            throw fe;
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(MessageService.class).warn("Failed to check block status for sender {} in chat {}", senderId, chatId, e);
        }
    }

    public MessageDto sendMessage(String senderId, SendMessageRequest request) {
        checkBlockStatus(senderId, request.getChatId());
        String messageId = UUIDv7Utils.generateString();
        MessageDocument doc = new MessageDocument(
                messageId,
                request.getChatId(),
                senderId,
                request.getContent(),
                request.getType(),
                request.getMediaUrl(),
                request.getReplyToMessageId()
        );

        if ("POLL".equalsIgnoreCase(request.getType())) {
            doc.setPollQuestion(request.getContent());
            if (request.getPollOptions() != null) {
                doc.setPollOptions(request.getPollOptions());
            }
        }

        messageRepository.save(doc);

        saveOutboxEvent(EventTopics.MESSAGE_SENT, messageId, Map.of(
                "messageId", messageId,
                "chatId", request.getChatId(),
                "senderId", senderId,
                "content", request.getContent() != null ? request.getContent() : "",
                "type", doc.getType()
        ));

        return toDto(doc);
    }

    public Page<MessageDto> getChatMessages(String userId, String chatId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Instant leftAt = null;
        try {
            String chatMemberUrl = "http://localhost:8083/internal/v1/chats/" + chatId + "/members/" + userId;
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("X-Internal-Token", "secret-internal-service-token");
            org.springframework.http.HttpEntity<Void> entity = new org.springframework.http.HttpEntity<>(headers);
            org.springframework.http.ResponseEntity<Map> response = restTemplate.exchange(
                    chatMemberUrl,
                    org.springframework.http.HttpMethod.GET,
                    entity,
                    Map.class
            );
            Map<?, ?> member = response.getBody();
            if (member != null) {
                Boolean active = (Boolean) member.get("active");
                String leftAtStr = (String) member.get("leftAt");
                if (Boolean.FALSE.equals(active) && leftAtStr != null) {
                    leftAt = Instant.parse(leftAtStr);
                }
            }
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(MessageService.class).warn("Failed to check chat member status for user {} in chat {}", userId, chatId, e);
        }

        if (leftAt != null) {
            return messageRepository.findByChatIdAndCreatedAtLessThanEqualOrderByCreatedAtDesc(chatId, leftAt, pageable)
                    .map(this::toDto);
        }

        return messageRepository.findByChatIdOrderByCreatedAtDesc(chatId, pageable)
                .map(this::toDto);
    }

    public MessageDto editMessage(String userId, String messageId, EditMessageRequest request) {
        MessageDocument doc = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found", "MESSAGE_NOT_FOUND"));

        if (!doc.getSenderId().equals(userId)) {
            throw new ForbiddenException("Not authorized to edit this message", "UNAUTHORIZED");
        }

        java.time.Instant limitTime = java.time.Instant.now().minus(java.time.Duration.ofMinutes(3));
        if (doc.getCreatedAt().isBefore(limitTime)) {
            throw new com.company.chatplatform.common.core.exception.BadRequestException("Edit time window (3 minutes) has expired", "TIME_WINDOW_EXPIRED");
        }

        doc.setContent(request.getContent());
        if ("POLL".equalsIgnoreCase(doc.getType())) {
            doc.setPollQuestion(request.getContent());
        }
        doc.setEdited(true);
        doc.setUpdatedAt(java.time.Instant.now());
        messageRepository.save(doc);

        saveOutboxEvent(EventTopics.MESSAGE_EDITED, messageId, Map.of("messageId", messageId, "chatId", doc.getChatId(), "content", request.getContent()));

        return toDto(doc);
    }

    public void deleteMessage(String userId, String messageId) {
        MessageDocument doc = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found", "MESSAGE_NOT_FOUND"));

        if (!doc.getSenderId().equals(userId)) {
            throw new ForbiddenException("Not authorized to delete this message", "UNAUTHORIZED");
        }

        java.time.Instant limitTime = java.time.Instant.now().minus(java.time.Duration.ofMinutes(3));
        if (doc.getCreatedAt().isBefore(limitTime)) {
            throw new com.company.chatplatform.common.core.exception.BadRequestException("Delete time window (3 minutes) has expired", "TIME_WINDOW_EXPIRED");
        }

        doc.setDeleted(true);
        doc.setContent("[Message deleted]");
        doc.setUpdatedAt(java.time.Instant.now());
        messageRepository.save(doc);

        saveOutboxEvent(EventTopics.MESSAGE_DELETED, messageId, Map.of("messageId", messageId, "chatId", doc.getChatId()));
    }

    public MessageDto addReaction(String userId, String messageId, AddReactionRequest request) {
        MessageDocument doc = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found", "MESSAGE_NOT_FOUND"));

        doc.getReactions().removeIf(r -> r.getUserId().equals(userId));
        doc.getReactions().add(new ReactionDocument(userId, request.getEmoji()));
        doc.setUpdatedAt(Instant.now());
        messageRepository.save(doc);

        return toDto(doc);
    }

    @Transactional
    public void markAsRead(String userId, String messageId) {
        MessageDocument doc = messageRepository.findById(messageId).orElse(null);
        if (doc != null) {
            if (!doc.getSenderId().equals(userId)) {
                boolean exists = doc.getReadReceipts().stream().anyMatch(r -> r.getUserId().equals(userId));
                if (!exists) {
                    doc.getReadReceipts().add(new ReadReceiptDocument(userId));
                    messageRepository.save(doc);
                    saveOutboxEvent("message.read.v1", doc.getId(), Map.of(
                            "messageId", doc.getId(),
                            "chatId", doc.getChatId(),
                            "userId", userId,
                            "readCount", doc.getReadReceipts().size()
                    ));
                }
            }
        }
    }

    public java.util.Map<String, Long> getUnreadCounts(String userId) {
        List<MessageDocument> unread = messageRepository.findUnreadMessagesForUser(userId);
        return unread.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        MessageDocument::getChatId,
                        java.util.stream.Collectors.counting()
                ));
    }

    @Transactional
    public void markChatAsRead(String userId, String chatId) {
        List<MessageDocument> unread = messageRepository.findUnreadMessagesForChat(userId, chatId);
        for (MessageDocument doc : unread) {
            doc.getReadReceipts().add(new ReadReceiptDocument(userId));
            if (doc.getDeliveryReceipts() == null) {
                doc.setDeliveryReceipts(new ArrayList<>());
            }
            boolean delivered = doc.getDeliveryReceipts().stream().anyMatch(d -> d.getUserId().equals(userId));
            if (!delivered) {
                doc.getDeliveryReceipts().add(new DeliveryReceiptDocument(userId));
            }
            messageRepository.save(doc);
            saveOutboxEvent("message.read.v1", doc.getId(), Map.of(
                    "messageId", doc.getId(),
                    "chatId", chatId,
                    "userId", userId,
                    "readCount", doc.getReadReceipts().size()
            ));
        }
    }

    @Transactional
    public void markChatAsDelivered(String userId, String chatId) {
        List<MessageDocument> undelivered = messageRepository.findUndeliveredMessagesForChat(userId, chatId);
        for (MessageDocument doc : undelivered) {
            if (doc.getDeliveryReceipts() == null) {
                doc.setDeliveryReceipts(new ArrayList<>());
            }
            boolean exists = doc.getDeliveryReceipts().stream().anyMatch(d -> d.getUserId().equals(userId));
            if (!exists) {
                doc.getDeliveryReceipts().add(new DeliveryReceiptDocument(userId));
                messageRepository.save(doc);
                saveOutboxEvent("message.delivered.v1", doc.getId(), Map.of(
                        "messageId", doc.getId(),
                        "chatId", chatId,
                        "userId", userId
                ));
            }
        }
    }

    public MessageDto pinMessage(String userId, String messageId, boolean pinned) {
        MessageDocument doc = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found", "MESSAGE_NOT_FOUND"));

        doc.setPinned(pinned);
        doc.setUpdatedAt(Instant.now());
        messageRepository.save(doc);

        saveOutboxEvent(EventTopics.MESSAGE_EDITED, messageId, Map.of(
                "messageId", messageId,
                "chatId", doc.getChatId(),
                "pinned", pinned
        ));

        return toDto(doc);
    }

    public MessageDto votePoll(String userId, String messageId, VotePollRequest request) {
        MessageDocument doc = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found", "MESSAGE_NOT_FOUND"));

        if (!"POLL".equalsIgnoreCase(doc.getType())) {
            throw new com.company.chatplatform.common.core.exception.BadRequestException("Message is not a poll", "NOT_A_POLL");
        }

        if (doc.getPollVotes() == null) {
            doc.setPollVotes(new ArrayList<>());
        }

        doc.getPollVotes().removeIf(v -> v.getUserId().equals(userId));
        doc.getPollVotes().add(new com.company.chatplatform.messageservice.domain.document.PollVote(userId, request.getOptionIndex()));
        doc.setUpdatedAt(Instant.now());
        messageRepository.save(doc);

        saveOutboxEvent(EventTopics.MESSAGE_EDITED, messageId, Map.of(
                "messageId", messageId,
                "chatId", doc.getChatId(),
                "votedUserId", userId,
                "optionIndex", request.getOptionIndex()
        ));

        return toDto(doc);
    }

    public List<MessageDto> getPinnedMessages(String chatId) {
        return messageRepository.findByChatIdAndPinnedTrue(chatId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    private MessageDto toDto(MessageDocument doc) {
        List<ReactionDto> reactions = doc.getReactions() != null ? doc.getReactions().stream()
                .map(r -> new ReactionDto(r.getUserId(), r.getEmoji(), r.getCreatedAt().toString()))
                .toList() : new ArrayList<>();

        List<PollVoteDto> pollVoteDtos = doc.getPollVotes() != null ? doc.getPollVotes().stream()
                .map(v -> new PollVoteDto(v.getUserId(), v.getOptionIndex()))
                .toList() : new ArrayList<>();

        List<ReadReceiptDto> readReceiptDtos = doc.getReadReceipts() != null ? doc.getReadReceipts().stream()
                .map(r -> new ReadReceiptDto(r.getUserId(), r.getReadAt().toString()))
                .toList() : new ArrayList<>();

        List<DeliveryReceiptDto> deliveryReceiptDtos = doc.getDeliveryReceipts() != null ? doc.getDeliveryReceipts().stream()
                .map(d -> new DeliveryReceiptDto(d.getUserId(), d.getDeliveredAt().toString()))
                .toList() : new ArrayList<>();

        int readCount = doc.getReadReceipts() != null ? doc.getReadReceipts().size() : 0;

        return new MessageDto(
                doc.getId(),
                doc.getChatId(),
                doc.getSenderId(),
                doc.getContent(),
                doc.getType(),
                doc.getMediaUrl(),
                doc.getReplyToMessageId(),
                doc.isEdited(),
                doc.isDeleted(),
                doc.isPinned(),
                doc.getPollQuestion(),
                doc.getPollOptions(),
                pollVoteDtos,
                reactions,
                readReceiptDtos,
                deliveryReceiptDtos,
                readCount,
                doc.getCreatedAt().toString(),
                doc.getUpdatedAt().toString()
        );
    }

    private void saveOutboxEvent(String eventType, String aggregateId, Map<String, Object> payload) {
        try {
            String payloadJson = objectMapper.writeValueAsString(payload);
            OutboxMessageDocument outbox = new OutboxMessageDocument(
                    UUIDv7Utils.generateString(),
                    "MESSAGE",
                    aggregateId,
                    eventType,
                    payloadJson
            );
            outboxMessageRepository.save(outbox);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize outbox event payload", e);
        }
    }

    public MessageDto saveInternalMessage(String senderId, SendMessageRequest request) {
        if (!"018f98d0-0000-0000-0000-000000000000".equals(senderId)) {
            throw new ForbiddenException("Only the system Aura Assistant can post through this internal endpoint.", "FORBIDDEN");
        }
        
        String messageId = UUIDv7Utils.generateString();
        MessageDocument doc = new MessageDocument(
                messageId,
                request.getChatId(),
                senderId,
                request.getContent(),
                request.getType() != null ? request.getType() : "TEXT",
                request.getMediaUrl(),
                request.getReplyToMessageId()
        );

        messageRepository.save(doc);

        saveOutboxEvent(EventTopics.MESSAGE_SENT, messageId, Map.of(
                "messageId", messageId,
                "chatId", request.getChatId(),
                "senderId", senderId,
                "content", request.getContent() != null ? request.getContent() : "",
                "type", doc.getType()
        ));

        return toDto(doc);
    }

    public List<MessageDto> getInternalChatMessages(String chatId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return messageRepository.findByChatIdOrderByCreatedAtDesc(chatId, pageable)
                .getContent().stream()
                .map(this::toDto)
                .toList();
    }
}
