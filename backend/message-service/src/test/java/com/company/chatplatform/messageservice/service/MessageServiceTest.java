package com.company.chatplatform.messageservice.service;

import com.company.chatplatform.messageservice.domain.document.MessageDocument;
import com.company.chatplatform.messageservice.domain.repository.MessageRepository;
import com.company.chatplatform.messageservice.domain.repository.OutboxMessageRepository;
import com.company.chatplatform.messageservice.dto.MessageDto;
import com.company.chatplatform.messageservice.dto.SendMessageRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

class MessageServiceTest {

    private MessageRepository messageRepository;
    private OutboxMessageRepository outboxMessageRepository;
    private ObjectMapper objectMapper;
    private MessageService messageService;

    @BeforeEach
    void setUp() {
        messageRepository = Mockito.mock(MessageRepository.class);
        outboxMessageRepository = Mockito.mock(OutboxMessageRepository.class);
        objectMapper = new ObjectMapper();

        messageService = new MessageService(messageRepository, outboxMessageRepository, objectMapper);
    }

    @Test
    void sendMessage_Success() {
        SendMessageRequest request = new SendMessageRequest("chat-1", "Hello World!", "TEXT", null, null);

        MessageDto result = messageService.sendMessage("user-1", request);

        assertNotNull(result);
        assertEquals("chat-1", result.getChatId());
        assertEquals("user-1", result.getSenderId());
        assertEquals("Hello World!", result.getContent());

        Mockito.verify(messageRepository, Mockito.times(1)).save(any(MessageDocument.class));
        Mockito.verify(outboxMessageRepository, Mockito.times(1)).save(any());
    }
}
