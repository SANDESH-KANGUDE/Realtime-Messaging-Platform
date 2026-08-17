package com.company.chatplatform.chatservice.controller;

import com.company.chatplatform.chatservice.service.ChatService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/internal/v1/chats")
public class InternalChatController {

    private final ChatService chatService;

    public InternalChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/{chatId}/member-ids")
    public ResponseEntity<?> getChatMemberIds(
            @RequestHeader(value = "X-Internal-Token", required = false) String internalToken,
            @PathVariable("chatId") String chatId
    ) {
        if (internalToken == null || !internalToken.equals("secret-internal-service-token")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized internal request");
        }
        List<String> memberIds = chatService.getChatMemberIds(chatId);
        return ResponseEntity.ok(memberIds);
    }
}
