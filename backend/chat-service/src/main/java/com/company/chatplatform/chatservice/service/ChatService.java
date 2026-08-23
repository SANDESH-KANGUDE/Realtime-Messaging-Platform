package com.company.chatplatform.chatservice.service;

import com.company.chatplatform.chatservice.domain.entity.ChatEntity;
import com.company.chatplatform.chatservice.domain.entity.ChatMemberEntity;
import com.company.chatplatform.chatservice.domain.entity.OutboxEventEntity;
import com.company.chatplatform.chatservice.domain.repository.ChatMemberRepository;
import com.company.chatplatform.chatservice.domain.repository.ChatRepository;
import com.company.chatplatform.chatservice.domain.repository.OutboxEventRepository;
import com.company.chatplatform.chatservice.dto.*;
import com.company.chatplatform.common.core.exception.ConflictException;
import com.company.chatplatform.common.core.exception.ResourceNotFoundException;
import com.company.chatplatform.common.core.util.UUIDv7Utils;
import com.company.chatplatform.common.kafka.topics.EventTopics;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.time.Instant;

@Service
public class ChatService {

    private final ChatRepository chatRepository;
    private final ChatMemberRepository chatMemberRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public ChatService(
            ChatRepository chatRepository,
            ChatMemberRepository chatMemberRepository,
            OutboxEventRepository outboxEventRepository,
            ObjectMapper objectMapper
    ) {
        this.chatRepository = chatRepository;
        this.chatMemberRepository = chatMemberRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ChatDto createOrGetDirectChat(String currentUserId, String targetUserId) {
        if (currentUserId.equals(targetUserId)) {
            throw new ConflictException("Cannot create a direct chat with yourself", "SELF_CHAT_NOT_ALLOWED");
        }

        Optional<ChatEntity> existing = chatRepository.findDirectChatBetween(currentUserId, targetUserId);
        if (existing.isPresent()) {
            return toDto(existing.get());
        }

        String chatId = UUIDv7Utils.generateString();
        ChatEntity chat = new ChatEntity(chatId, "DIRECT", null, null, currentUserId);
        chatRepository.save(chat);

        ChatMemberEntity m1 = new ChatMemberEntity(UUIDv7Utils.generateString(), chatId, currentUserId, "MEMBER");
        ChatMemberEntity m2 = new ChatMemberEntity(UUIDv7Utils.generateString(), chatId, targetUserId, "MEMBER");
        chatMemberRepository.save(m1);
        chatMemberRepository.save(m2);

        saveOutboxEvent(EventTopics.CHAT_CREATED, chatId, Map.of("chatId", chatId, "type", "DIRECT", "members", List.of(currentUserId, targetUserId)));

        return toDto(chat);
    }

    @Transactional
    public ChatDto createGroupChat(String currentUserId, CreateGroupChatRequest request) {
        String chatId = UUIDv7Utils.generateString();
        ChatEntity chat = new ChatEntity(chatId, "GROUP", request.getTitle(), request.getAvatarUrl(), currentUserId);
        chatRepository.save(chat);

        ChatMemberEntity owner = new ChatMemberEntity(UUIDv7Utils.generateString(), chatId, currentUserId, "OWNER");
        chatMemberRepository.save(owner);

        for (String memberId : request.getMemberUserIds()) {
            if (!memberId.equals(currentUserId)) {
                ChatMemberEntity member = new ChatMemberEntity(UUIDv7Utils.generateString(), chatId, memberId, "MEMBER");
                chatMemberRepository.save(member);
            }
        }

        saveOutboxEvent(EventTopics.CHAT_CREATED, chatId, Map.of("chatId", chatId, "type", "GROUP", "title", request.getTitle()));

        return toDto(chat);
    }

    public List<ChatDto> getUserChats(String userId) {
        List<ChatEntity> chats = chatRepository.findAllChatsForUser(userId);
        return chats.stream().map(c -> toDto(c, userId)).toList();
    }

    public ChatDto getChatDetails(String chatId, String userId) {
        ChatEntity chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat not found", "CHAT_NOT_FOUND"));

        if (!chatMemberRepository.existsByChatIdAndUserId(chatId, userId)) {
            throw new ResourceNotFoundException("User is not a member of this chat", "NOT_A_CHAT_MEMBER");
        }

        return toDto(chat, userId);
    }

    @Transactional
    public ChatMemberDto addMember(String currentUserId, String chatId, AddMemberRequest request) {
        ChatEntity chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat not found", "CHAT_NOT_FOUND"));

        ChatMemberEntity requester = chatMemberRepository.findByChatIdAndUserId(chatId, currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Not authorized to add members to this chat", "UNAUTHORIZED"));

        if (!"OWNER".equals(requester.getRole()) && !"ADMIN".equals(requester.getRole())) {
            throw new com.company.chatplatform.common.core.exception.ForbiddenException("Only owners or admins can add members", "FORBIDDEN");
        }

        Optional<ChatMemberEntity> existing = chatMemberRepository.findByChatIdAndUserId(chatId, request.getUserId());
        ChatMemberEntity member;
        if (existing.isPresent()) {
            member = existing.get();
            if (member.isActive()) {
                throw new ConflictException("User is already a member of this chat", "MEMBER_ALREADY_EXISTS");
            }
            member.setActive(true);
            member.setLeftAt(null);
            member.setRole(request.getRole() != null ? request.getRole() : "MEMBER");
            chatMemberRepository.save(member);
        } else {
            String memberId = UUIDv7Utils.generateString();
            member = new ChatMemberEntity(memberId, chatId, request.getUserId(), request.getRole());
            chatMemberRepository.save(member);
        }

        saveOutboxEvent(EventTopics.GROUP_UPDATED, chatId, Map.of("chatId", chatId, "action", "MEMBER_ADDED", "userId", request.getUserId()));

        return new ChatMemberDto(member.getId(), member.getChatId(), member.getUserId(), member.getRole(), member.isPinned(), member.getJoinedAt().toString());
    }

    @Transactional
    public void removeMember(String currentUserId, String chatId, String targetUserId) {
        ChatMemberEntity requester = chatMemberRepository.findByChatIdAndUserId(chatId, currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Not authorized", "UNAUTHORIZED"));

        if (!currentUserId.equals(targetUserId)) {
            if (!"OWNER".equals(requester.getRole()) && !"ADMIN".equals(requester.getRole())) {
                throw new com.company.chatplatform.common.core.exception.ForbiddenException("Only owners or admins can kick members", "FORBIDDEN");
            }
        }

        ChatMemberEntity targetMember = chatMemberRepository.findByChatIdAndUserId(chatId, targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Target member not found", "MEMBER_NOT_FOUND"));

        targetMember.setActive(false);
        targetMember.setLeftAt(Instant.now());
        chatMemberRepository.save(targetMember);

        saveOutboxEvent(EventTopics.GROUP_UPDATED, chatId, Map.of("chatId", chatId, "action", "MEMBER_REMOVED", "userId", targetUserId));
    }

    @Transactional
    public ChatMemberDto updateMemberRole(String currentUserId, String chatId, String targetUserId, String newRole) {
        ChatMemberEntity currentMember = chatMemberRepository.findByChatId(chatId).stream()
                .filter(m -> m.getUserId().equals(currentUserId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Not authorized", "UNAUTHORIZED"));

        if (!"OWNER".equals(currentMember.getRole()) && !"ADMIN".equals(currentMember.getRole())) {
            throw new com.company.chatplatform.common.core.exception.ForbiddenException("Only owner or admin can update member roles", "FORBIDDEN");
        }

        ChatMemberEntity targetMember = chatMemberRepository.findByChatId(chatId).stream()
                .filter(m -> m.getUserId().equals(targetUserId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Target member not found in chat", "MEMBER_NOT_FOUND"));

        targetMember.setRole(newRole);
        chatMemberRepository.save(targetMember);

        saveOutboxEvent(EventTopics.GROUP_UPDATED, chatId, Map.of(
                "chatId", chatId,
                "action", "ROLE_UPDATED",
                "userId", targetUserId,
                "role", newRole
        ));

        return new ChatMemberDto(targetMember.getId(), targetMember.getChatId(), targetMember.getUserId(), targetMember.getRole(), targetMember.isPinned(), targetMember.getJoinedAt().toString());
    }

    @Transactional
    public void pinChat(String currentUserId, String chatId, boolean pinned) {
        ChatMemberEntity member = chatMemberRepository.findByChatId(chatId).stream()
                .filter(m -> m.getUserId().equals(currentUserId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Not authorized", "UNAUTHORIZED"));

        member.setPinned(pinned);
        chatMemberRepository.save(member);
    }

    private ChatDto toDto(ChatEntity chat) {
        return toDto(chat, null);
    }

    private ChatDto toDto(ChatEntity chat, String currentUserId) {
        List<ChatMemberDto> memberDtos = chatMemberRepository.findByChatId(chat.getId())
                .stream()
                .map(m -> new ChatMemberDto(
                        m.getId(),
                        m.getChatId(),
                        m.getUserId(),
                        m.getRole(),
                        m.isPinned(),
                        m.isArchived(),
                        m.getTheme(),
                        m.getLeftAt() != null ? m.getLeftAt().toString() : null,
                        m.isActive(),
                        m.getJoinedAt().toString()
                ))
                .toList();

        boolean pinned = false;
        boolean archived = false;
        if (currentUserId != null) {
            ChatMemberDto currentMember = memberDtos.stream()
                    .filter(m -> m.getUserId().equals(currentUserId))
                    .findFirst()
                    .orElse(null);
            if (currentMember != null) {
                pinned = currentMember.isPinned();
                archived = currentMember.isArchived();
            }
        }

        return new ChatDto(
                chat.getId(),
                chat.getType(),
                chat.getTitle(),
                chat.getAvatarUrl(),
                chat.getCreatedBy(),
                memberDtos,
                pinned,
                archived,
                chat.getCreatedAt().toString(),
                chat.getUpdatedAt().toString()
        );
    }

    private void saveOutboxEvent(String eventType, String aggregateId, Map<String, Object> payload) {
        try {
            String payloadJson = objectMapper.writeValueAsString(payload);
            OutboxEventEntity event = new OutboxEventEntity(
                    UUIDv7Utils.generateString(),
                    "CHAT",
                    aggregateId,
                    eventType,
                    payloadJson
            );
            outboxEventRepository.save(event);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize outbox event payload", e);
        }
    }

    public List<String> getChatMemberIds(String chatId) {
        return chatMemberRepository.findByChatId(chatId).stream()
                .map(ChatMemberEntity::getUserId)
                .toList();
    }

    public List<String> getActiveMemberIds(String chatId) {
        return chatMemberRepository.findByChatId(chatId).stream()
                .filter(m -> !m.isArchived())
                .map(ChatMemberEntity::getUserId)
                .toList();
    }

    @Transactional
    public void archiveChat(String userId, String chatId, boolean archived) {
        ChatMemberEntity member = chatMemberRepository.findByChatIdAndUserId(chatId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found", "MEMBER_NOT_FOUND"));
        member.setArchived(archived);
        chatMemberRepository.save(member);
    }

    @Transactional
    public void updateChatTheme(String userId, String chatId, String theme) {
        ChatMemberEntity member = chatMemberRepository.findByChatIdAndUserId(chatId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found", "MEMBER_NOT_FOUND"));
        member.setTheme(theme);
        chatMemberRepository.save(member);
    }

    @Transactional
    public void deleteGroupChat(String userId, String chatId) {
        ChatEntity chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat not found", "CHAT_NOT_FOUND"));

        if (!"GROUP".equals(chat.getType())) {
            throw new ConflictException("Only group chats can be deleted", "NOT_A_GROUP");
        }

        if (!chat.getCreatedBy().equals(userId)) {
            throw new com.company.chatplatform.common.core.exception.ForbiddenException("Only the creator of the group can delete it", "FORBIDDEN");
        }

        chatRepository.delete(chat);
        chatMemberRepository.findByChatId(chatId).forEach(chatMemberRepository::delete);
        saveOutboxEvent(EventTopics.GROUP_UPDATED, chatId, Map.of("chatId", chatId, "action", "DELETED"));
    }

    public ChatMemberDto getChatMember(String chatId, String userId) {
        ChatMemberEntity m = chatMemberRepository.findByChatIdAndUserId(chatId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found", "MEMBER_NOT_FOUND"));
        return new ChatMemberDto(
                m.getId(),
                m.getChatId(),
                m.getUserId(),
                m.getRole(),
                m.isPinned(),
                m.isArchived(),
                m.getTheme(),
                m.getLeftAt() != null ? m.getLeftAt().toString() : null,
                m.isActive(),
                m.getJoinedAt().toString()
        );
    }
}
