package com.company.chatplatform.messageservice.controller;

import com.company.chatplatform.messageservice.dto.MessageDto;
import com.company.chatplatform.messageservice.dto.SendMessageRequest;
import com.company.chatplatform.messageservice.service.MessageService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/internal/v1/messages")
public class InternalMessageController {

    private final MessageService messageService;

    public InternalMessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping("/chat/{chatId}")
    public ResponseEntity<?> getInternalChatMessages(
            @RequestHeader(value = "X-Internal-Token", required = false) String internalToken,
            @PathVariable("chatId") String chatId,
            @RequestParam(value = "limit", defaultValue = "20") int limit
    ) {
        if (internalToken == null || !internalToken.equals("secret-internal-service-token")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized internal request");
        }
        List<MessageDto> messages = messageService.getInternalChatMessages(chatId, limit);
        return ResponseEntity.ok(messages);
    }

    @PostMapping
    public ResponseEntity<?> saveInternalMessage(
            @RequestHeader(value = "X-Internal-Token", required = false) String internalToken,
            @RequestHeader(value = "X-Internal-Sender-Id", required = false) String senderId,
            @Valid @RequestBody SendMessageRequest request
    ) {
        if (internalToken == null || !internalToken.equals("secret-internal-service-token")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized internal request");
        }
        try {
            MessageDto message = messageService.saveInternalMessage(senderId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(message);
        } catch (com.company.chatplatform.common.core.exception.ForbiddenException fe) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(fe.getMessage());
        }
    }
}
