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
}
