package com.company.chatplatform.chatservice.service;

import com.company.chatplatform.chatservice.domain.entity.ChatEntity;
import com.company.chatplatform.chatservice.domain.repository.ChatMemberRepository;
import com.company.chatplatform.chatservice.domain.repository.ChatRepository;
import com.company.chatplatform.chatservice.domain.repository.OutboxEventRepository;
import com.company.chatplatform.chatservice.dto.ChatDto;
import com.company.chatplatform.chatservice.dto.CreateGroupChatRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

class ChatServiceTest {

    private ChatRepository chatRepository;
    private ChatMemberRepository chatMemberRepository;
    private OutboxEventRepository outboxEventRepository;
    private ObjectMapper objectMapper;
    private ChatService chatService;

    @BeforeEach
    void setUp() {
        chatRepository = Mockito.mock(ChatRepository.class);
        chatMemberRepository = Mockito.mock(ChatMemberRepository.class);
        outboxEventRepository = Mockito.mock(OutboxEventRepository.class);
        objectMapper = new ObjectMapper();

        chatService = new ChatService(chatRepository, chatMemberRepository, outboxEventRepository, objectMapper);
    }

    @Test
    void createOrGetDirectChat_NewChat_Success() {
        Mockito.when(chatRepository.findDirectChatBetween("user1", "user2")).thenReturn(Optional.empty());

        ChatDto chat = chatService.createOrGetDirectChat("user1", "user2");

        assertNotNull(chat);
        assertEquals("DIRECT", chat.getType());
        Mockito.verify(chatRepository, Mockito.times(1)).save(any());
        Mockito.verify(chatMemberRepository, Mockito.times(2)).save(any());
        Mockito.verify(outboxEventRepository, Mockito.times(1)).save(any());
    }

    @Test
    void createGroupChat_Success() {
        CreateGroupChatRequest req = new CreateGroupChatRequest("Dev Team", null, List.of("user2", "user3"));

        ChatDto group = chatService.createGroupChat("user1", req);

        assertNotNull(group);
        assertEquals("GROUP", group.getType());
        assertEquals("Dev Team", group.getTitle());
    }
}
