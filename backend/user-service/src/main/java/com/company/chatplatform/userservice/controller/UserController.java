package com.company.chatplatform.userservice.controller;

import com.company.chatplatform.common.core.dto.ApiResponse;
import com.company.chatplatform.common.security.context.UserContextHolder;
import com.company.chatplatform.userservice.dto.UpdateProfileRequest;
import com.company.chatplatform.userservice.dto.UserProfileDto;
import com.company.chatplatform.userservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileDto>> getMyProfile(@RequestHeader(value = "X-User-Id", required = false) String headerUserId) {
        String userId = headerUserId != null ? headerUserId : UserContextHolder.getUserId();
        UserProfileDto profile = userService.getProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @PutMapping("/me/profile")
    public ResponseEntity<ApiResponse<UserProfileDto>> updateProfile(
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        String userId = headerUserId != null ? headerUserId : UserContextHolder.getUserId();
        UserProfileDto updated = userService.updateProfile(userId, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Profile updated successfully"));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserProfileDto>> getUserProfile(@PathVariable("userId") String userId) {
        UserProfileDto profile = userService.getProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<UserProfileDto>>> searchUsers(@RequestParam("query") String query) {
        List<UserProfileDto> results = userService.searchUsers(query);
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    @PostMapping("/block/{targetUserId}")
    public ResponseEntity<ApiResponse<Void>> blockUser(
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
            @PathVariable("targetUserId") String targetUserId
    ) {
        String userId = headerUserId != null ? headerUserId : UserContextHolder.getUserId();
        userService.blockUser(userId, targetUserId);
        return ResponseEntity.ok(ApiResponse.success(null, "User blocked successfully"));
    }

    @PostMapping("/unblock/{targetUserId}")
    public ResponseEntity<ApiResponse<Void>> unblockUser(
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
            @PathVariable("targetUserId") String targetUserId
    ) {
        String userId = headerUserId != null ? headerUserId : UserContextHolder.getUserId();
        userService.unblockUser(userId, targetUserId);
        return ResponseEntity.ok(ApiResponse.success(null, "User unblocked successfully"));
    }

    @GetMapping("/blocked")
    public ResponseEntity<ApiResponse<List<String>>> getBlockedUsers(
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId
    ) {
        String userId = headerUserId != null ? headerUserId : UserContextHolder.getUserId();
        List<String> blockedUserIds = userService.getBlockedUserIds(userId);
        return ResponseEntity.ok(ApiResponse.success(blockedUserIds));
    }

    @GetMapping("/internal/is-blocked")
    public ResponseEntity<Boolean> isBlocked(
            @RequestParam("user1") String user1,
            @RequestParam("user2") String user2
    ) {
        boolean blocked = userService.isBlocked(user1, user2) || userService.isBlocked(user2, user1);
        return ResponseEntity.ok(blocked);
    }
}
