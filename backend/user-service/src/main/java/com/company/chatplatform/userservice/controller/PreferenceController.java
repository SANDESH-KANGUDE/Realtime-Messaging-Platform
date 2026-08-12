package com.company.chatplatform.userservice.controller;

import com.company.chatplatform.common.core.dto.ApiResponse;
import com.company.chatplatform.common.security.context.UserContextHolder;
import com.company.chatplatform.userservice.dto.UserPreferenceDto;
import com.company.chatplatform.userservice.service.UserPreferenceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/preferences")
public class PreferenceController {

    private final UserPreferenceService userPreferenceService;

    public PreferenceController(UserPreferenceService userPreferenceService) {
        this.userPreferenceService = userPreferenceService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<UserPreferenceDto>> getPreferences(@RequestHeader(value = "X-User-Id", required = false) String headerUserId) {
        String userId = headerUserId != null ? headerUserId : UserContextHolder.getUserId();
        UserPreferenceDto prefs = userPreferenceService.getPreferences(userId);
        return ResponseEntity.ok(ApiResponse.success(prefs));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<UserPreferenceDto>> updatePreferences(
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
            @RequestBody UserPreferenceDto request
    ) {
        String userId = headerUserId != null ? headerUserId : UserContextHolder.getUserId();
        UserPreferenceDto updated = userPreferenceService.updatePreferences(userId, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Preferences updated"));
    }
}
