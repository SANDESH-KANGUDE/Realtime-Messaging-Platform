package com.company.chatplatform.userservice.controller;

import com.company.chatplatform.common.core.dto.ApiResponse;
import com.company.chatplatform.common.security.context.UserContextHolder;
import com.company.chatplatform.userservice.dto.FriendRequestDto;
import com.company.chatplatform.userservice.dto.FriendshipDto;
import com.company.chatplatform.userservice.service.FriendshipService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users/friends")
public class FriendshipController {

    private final FriendshipService friendshipService;

    public FriendshipController(FriendshipService friendshipService) {
        this.friendshipService = friendshipService;
    }

    @PostMapping("/request")
    public ResponseEntity<ApiResponse<FriendshipDto>> sendRequest(
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
            @Valid @RequestBody FriendRequestDto request
    ) {
        String requesterId = headerUserId != null ? headerUserId : UserContextHolder.getUserId();
        FriendshipDto result = friendshipService.sendFriendRequest(requesterId, request.getAddresseeId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(result, "Friend request sent"));
    }

    @PutMapping("/request/{friendshipId}/accept")
    public ResponseEntity<ApiResponse<FriendshipDto>> acceptRequest(
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
            @PathVariable("friendshipId") String friendshipId
    ) {
        String userId = headerUserId != null ? headerUserId : UserContextHolder.getUserId();
        FriendshipDto result = friendshipService.acceptFriendRequest(userId, friendshipId);
        return ResponseEntity.ok(ApiResponse.success(result, "Friend request accepted"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<FriendshipDto>>> getFriends(@RequestHeader(value = "X-User-Id", required = false) String headerUserId) {
        String userId = headerUserId != null ? headerUserId : UserContextHolder.getUserId();
        List<FriendshipDto> friends = friendshipService.getUserFriends(userId);
        return ResponseEntity.ok(ApiResponse.success(friends));
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<FriendshipDto>>> getPendingRequests(@RequestHeader(value = "X-User-Id", required = false) String headerUserId) {
        String userId = headerUserId != null ? headerUserId : UserContextHolder.getUserId();
        List<FriendshipDto> pending = friendshipService.getPendingRequests(userId);
        return ResponseEntity.ok(ApiResponse.success(pending));
    }
}
