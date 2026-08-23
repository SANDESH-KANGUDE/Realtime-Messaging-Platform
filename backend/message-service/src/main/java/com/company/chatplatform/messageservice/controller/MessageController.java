package com.company.chatplatform.messageservice.controller;

import com.company.chatplatform.common.core.dto.ApiResponse;
import com.company.chatplatform.common.security.context.UserContextHolder;
import com.company.chatplatform.messageservice.dto.*;
import com.company.chatplatform.messageservice.service.MessageService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MessageDto>> sendMessage(
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
            @Valid @RequestBody SendMessageRequest request
    ) {
        String senderId = headerUserId != null ? headerUserId : UserContextHolder.getUserId();
        MessageDto message = messageService.sendMessage(senderId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(message, "Message sent"));
    }

    @GetMapping("/chat/{chatId}")
    public ResponseEntity<ApiResponse<Page<MessageDto>>> getChatMessages(
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
            @PathVariable("chatId") String chatId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "30") int size
    ) {
        String userId = headerUserId != null ? headerUserId : UserContextHolder.getUserId();
        Page<MessageDto> messages = messageService.getChatMessages(userId, chatId, page, size);
        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    @PutMapping("/{messageId}")
    public ResponseEntity<ApiResponse<MessageDto>> editMessage(
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
            @PathVariable("messageId") String messageId,
            @Valid @RequestBody EditMessageRequest request
    ) {
        String userId = headerUserId != null ? headerUserId : UserContextHolder.getUserId();
        MessageDto edited = messageService.editMessage(userId, messageId, request);
        return ResponseEntity.ok(ApiResponse.success(edited, "Message edited"));
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<ApiResponse<Void>> deleteMessage(
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
            @PathVariable("messageId") String messageId
    ) {
        String userId = headerUserId != null ? headerUserId : UserContextHolder.getUserId();
        messageService.deleteMessage(userId, messageId);
        return ResponseEntity.ok(ApiResponse.success(null, "Message deleted"));
    }

    @PostMapping("/{messageId}/reactions")
    public ResponseEntity<ApiResponse<MessageDto>> addReaction(
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
            @PathVariable("messageId") String messageId,
            @Valid @RequestBody AddReactionRequest request
    ) {
        String userId = headerUserId != null ? headerUserId : UserContextHolder.getUserId();
        MessageDto result = messageService.addReaction(userId, messageId, request);
        return ResponseEntity.ok(ApiResponse.success(result, "Reaction added"));
    }

    @PostMapping("/{messageId}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
            @PathVariable("messageId") String messageId
    ) {
        String userId = headerUserId != null ? headerUserId : UserContextHolder.getUserId();
        messageService.markAsRead(userId, messageId);
        return ResponseEntity.ok(ApiResponse.success(null, "Marked as read"));
    }

    @GetMapping("/unread")
    public ResponseEntity<ApiResponse<java.util.Map<String, Long>>> getUnreadCounts(
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId
    ) {
        String userId = headerUserId != null ? headerUserId : UserContextHolder.getUserId();
        java.util.Map<String, Long> counts = messageService.getUnreadCounts(userId);
        return ResponseEntity.ok(ApiResponse.success(counts));
    }

    @PostMapping("/chats/{chatId}/read")
    public ResponseEntity<ApiResponse<Void>> markChatAsRead(
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
            @PathVariable("chatId") String chatId
    ) {
        String userId = headerUserId != null ? headerUserId : UserContextHolder.getUserId();
        messageService.markChatAsRead(userId, chatId);
        return ResponseEntity.ok(ApiResponse.success(null, "Chat marked as read"));
    }

    @GetMapping("/chat/{chatId}/pinned")
    public ResponseEntity<ApiResponse<java.util.List<MessageDto>>> getPinnedMessages(
            @PathVariable("chatId") String chatId
    ) {
        java.util.List<MessageDto> pinned = messageService.getPinnedMessages(chatId);
        return ResponseEntity.ok(ApiResponse.success(pinned));
    }

    @PostMapping("/chats/{chatId}/deliver")
    public ResponseEntity<ApiResponse<Void>> markChatAsDelivered(
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
            @PathVariable("chatId") String chatId
    ) {
        String userId = headerUserId != null ? headerUserId : UserContextHolder.getUserId();
        messageService.markChatAsDelivered(userId, chatId);
        return ResponseEntity.ok(ApiResponse.success(null, "Chat marked as delivered"));
    }

    @PutMapping("/{messageId}/pin")
    public ResponseEntity<ApiResponse<MessageDto>> pinMessage(
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
            @PathVariable("messageId") String messageId
    ) {
        String userId = headerUserId != null ? headerUserId : UserContextHolder.getUserId();
        MessageDto result = messageService.pinMessage(userId, messageId, true);
        return ResponseEntity.ok(ApiResponse.success(result, "Message pinned"));
    }

    @PutMapping("/{messageId}/unpin")
    public ResponseEntity<ApiResponse<MessageDto>> unpinMessage(
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
            @PathVariable("messageId") String messageId
    ) {
        String userId = headerUserId != null ? headerUserId : UserContextHolder.getUserId();
        MessageDto result = messageService.pinMessage(userId, messageId, false);
        return ResponseEntity.ok(ApiResponse.success(result, "Message unpinned"));
    }

    @PostMapping("/{messageId}/poll/vote")
    public ResponseEntity<ApiResponse<MessageDto>> votePoll(
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
            @PathVariable("messageId") String messageId,
            @Valid @RequestBody VotePollRequest request
    ) {
        String userId = headerUserId != null ? headerUserId : UserContextHolder.getUserId();
        MessageDto result = messageService.votePoll(userId, messageId, request);
        return ResponseEntity.ok(ApiResponse.success(result, "Vote recorded"));
    }
}
