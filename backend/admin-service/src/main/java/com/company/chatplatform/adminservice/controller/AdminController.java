package com.company.chatplatform.adminservice.controller;

import com.company.chatplatform.adminservice.dto.*;
import com.company.chatplatform.adminservice.service.AdminService;
import com.company.chatplatform.common.core.dto.ApiResponse;
import com.company.chatplatform.common.security.context.UserContextHolder;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/reports")
    public ResponseEntity<ApiResponse<ModerationReportDto>> reportUser(
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
            @Valid @RequestBody ReportUserRequest request
    ) {
        String reporterId = headerUserId != null ? headerUserId : UserContextHolder.getUserId();
        ModerationReportDto report = adminService.createReport(reporterId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(report, "Report submitted"));
    }

    @GetMapping("/reports")
    public ResponseEntity<ApiResponse<List<ModerationReportDto>>> getReports() {
        List<ModerationReportDto> reports = adminService.getOpenReports();
        return ResponseEntity.ok(ApiResponse.success(reports));
    }

    @PostMapping("/users/ban")
    public ResponseEntity<ApiResponse<Void>> banUser(
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
            @Valid @RequestBody BanUserRequest request
    ) {
        String adminId = headerUserId != null ? headerUserId : UserContextHolder.getUserId();
        adminService.banUser(adminId, request);
        return ResponseEntity.ok(ApiResponse.success(null, "User banned successfully"));
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<ApiResponse<List<AuditLogDto>>> getAuditLogs() {
        List<AuditLogDto> logs = adminService.getAuditLogs();
        return ResponseEntity.ok(ApiResponse.success(logs));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> getDashboard(
            @RequestHeader(value = "X-User-Role", required = false) String role
    ) {
        if (role == null || !role.contains("ROLE_ADMIN")) {
            throw new com.company.chatplatform.common.core.exception.ForbiddenException("Standard User is forbidden from accessing Admin Dashboard", "FORBIDDEN");
        }
        return ResponseEntity.ok(ApiResponse.success(java.util.Map.of("status", "healthy", "reportsCount", 0)));
    }
}
