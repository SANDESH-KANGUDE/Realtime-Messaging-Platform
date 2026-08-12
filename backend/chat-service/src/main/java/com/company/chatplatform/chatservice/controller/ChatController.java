package com.company.chatplatform.chatservice.controller;

import com.company.chatplatform.chatservice.dto.*;
import com.company.chatplatform.chatservice.service.ChatService;
import com.company.chatplatform.common.core.dto.ApiResponse;
import com.company.chatplatform.common.security.context.UserContextHolder;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chats")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/direct")
    public ResponseEntity<ApiResponse<ChatDto>> createDirectChat(
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
            @Valid @RequestBody CreateDirectChatRequest request
    ) {
        String currentUserId = headerUserId != null ? headerUserId : UserContextHolder.getUserId();
        ChatDto chat = chatService.createOrGetDirectChat(currentUserId, request.getTargetUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(chat));
    }

    @PostMapping("/groups")
    public ResponseEntity<ApiResponse<ChatDto>> createGroupChat(
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
            @Valid @RequestBody CreateGroupChatRequest request
    ) {
        String currentUserId = headerUserId != null ? headerUserId : UserContextHolder.getUserId();
        ChatDto chat = chatService.createGroupChat(currentUserId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(chat, "Group chat created"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ChatDto>>> getMyChats(@RequestHeader(value = "X-User-Id", required = false) String headerUserId) {
        String currentUserId = headerUserId != null ? headerUserId : UserContextHolder.getUserId();
        List<ChatDto> chats = chatService.getUserChats(currentUserId);
        return ResponseEntity.ok(ApiResponse.success(chats));
    }

    @GetMapping("/{chatId}")
    public ResponseEntity<ApiResponse<ChatDto>> getChatDetails(
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
            @PathVariable("chatId") String chatId
    ) {
        String currentUserId = headerUserId != null ? headerUserId : UserContextHolder.getUserId();
        ChatDto chat = chatService.getChatDetails(chatId, currentUserId);
        return ResponseEntity.ok(ApiResponse.success(chat));
    }

    @PostMapping("/{chatId}/members")
    public ResponseEntity<ApiResponse<ChatMemberDto>> addMember(
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
            @PathVariable("chatId") String chatId,
            @Valid @RequestBody AddMemberRequest request
    ) {
        String currentUserId = headerUserId != null ? headerUserId : UserContextHolder.getUserId();
        ChatMemberDto member = chatService.addMember(chatId, currentUserId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(member, "Member added"));
    }

    @DeleteMapping("/{chatId}/members/{userId}")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
            @PathVariable("chatId") String chatId,
            @PathVariable("userId") String userId
    ) {
        String currentUserId = headerUserId != null ? headerUserId : UserContextHolder.getUserId();
        chatService.removeMember(currentUserId, chatId, userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Member removed"));
    }

    @PutMapping("/{chatId}/members/{userId}/role")
    public ResponseEntity<ApiResponse<ChatMemberDto>> updateMemberRole(
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
            @PathVariable("chatId") String chatId,
            @PathVariable("userId") String userId,
            @RequestParam("role") String role
    ) {
        String currentUserId = headerUserId != null ? headerUserId : UserContextHolder.getUserId();
        ChatMemberDto member = chatService.updateMemberRole(currentUserId, chatId, userId, role);
        return ResponseEntity.ok(ApiResponse.success(member, "Member role updated"));
    }

    @PutMapping("/{chatId}/pin")
    public ResponseEntity<ApiResponse<Void>> pinChat(
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
            @PathVariable("chatId") String chatId
    ) {
        String currentUserId = headerUserId != null ? headerUserId : UserContextHolder.getUserId();
        chatService.pinChat(currentUserId, chatId, true);
        return ResponseEntity.ok(ApiResponse.success(null, "Chat pinned"));
    }

    @PutMapping("/{chatId}/unpin")
    public ResponseEntity<ApiResponse<Void>> unpinChat(
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
            @PathVariable("chatId") String chatId
    ) {
        String currentUserId = headerUserId != null ? headerUserId : UserContextHolder.getUserId();
        chatService.pinChat(currentUserId, chatId, false);
        return ResponseEntity.ok(ApiResponse.success(null, "Chat unpinned"));
    }
}
