package com.company.chatplatform.messageservice.service;

import com.company.chatplatform.common.core.exception.ResourceNotFoundException;
import com.company.chatplatform.common.core.exception.ForbiddenException;
import com.company.chatplatform.common.core.util.UUIDv7Utils;
import com.company.chatplatform.common.kafka.topics.EventTopics;
import com.company.chatplatform.messageservice.domain.document.MessageDocument;
import com.company.chatplatform.messageservice.domain.document.OutboxMessageDocument;
import com.company.chatplatform.messageservice.domain.document.ReactionDocument;
import com.company.chatplatform.messageservice.domain.document.ReadReceiptDocument;
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

    public MessageDto sendMessage(String senderId, SendMessageRequest request) {
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

    public Page<MessageDto> getChatMessages(String chatId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
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
            messageRepository.save(doc);
            saveOutboxEvent("message.read.v1", doc.getId(), Map.of(
                    "messageId", doc.getId(),
                    "chatId", chatId,
                    "userId", userId,
                    "readCount", doc.getReadReceipts().size()
            ));
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
}
