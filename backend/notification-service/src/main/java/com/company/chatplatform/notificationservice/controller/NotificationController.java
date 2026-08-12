package com.company.chatplatform.notificationservice.controller;

import com.company.chatplatform.common.core.dto.ApiResponse;
import com.company.chatplatform.common.security.context.UserContextHolder;
import com.company.chatplatform.notificationservice.dto.NotificationDto;
import com.company.chatplatform.notificationservice.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationDto>>> getMyNotifications(@RequestHeader(value = "X-User-Id", required = false) String headerUserId) {
        String userId = headerUserId != null ? headerUserId : UserContextHolder.getUserId();
        List<NotificationDto> notifications = notificationService.getUserNotifications(userId);
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<NotificationDto>> markAsRead(
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
            @PathVariable("id") String id
    ) {
        String userId = headerUserId != null ? headerUserId : UserContextHolder.getUserId();
        NotificationDto notification = notificationService.markAsRead(id, userId);
        return ResponseEntity.ok(ApiResponse.success(notification, "Notification marked as read"));
    }
}
