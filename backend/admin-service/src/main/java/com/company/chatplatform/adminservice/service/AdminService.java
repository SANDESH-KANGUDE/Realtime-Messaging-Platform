package com.company.chatplatform.adminservice.service;

import com.company.chatplatform.adminservice.domain.entity.AuditLogEntity;
import com.company.chatplatform.adminservice.domain.entity.ModerationReportEntity;
import com.company.chatplatform.adminservice.domain.entity.OutboxEventEntity;
import com.company.chatplatform.adminservice.domain.repository.AuditLogRepository;
import com.company.chatplatform.adminservice.domain.repository.ModerationReportRepository;
import com.company.chatplatform.adminservice.domain.repository.OutboxEventRepository;
import com.company.chatplatform.adminservice.dto.*;
import com.company.chatplatform.common.core.exception.ResourceNotFoundException;
import com.company.chatplatform.common.core.util.UUIDv7Utils;
import com.company.chatplatform.common.kafka.topics.EventTopics;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class AdminService {

    private final ModerationReportRepository reportRepository;
    private final AuditLogRepository auditLogRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public AdminService(ModerationReportRepository reportRepository, AuditLogRepository auditLogRepository, OutboxEventRepository outboxEventRepository, ObjectMapper objectMapper) {
        this.reportRepository = reportRepository;
        this.auditLogRepository = auditLogRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ModerationReportDto createReport(String reporterId, ReportUserRequest request) {
        ModerationReportEntity report = new ModerationReportEntity(
                UUIDv7Utils.generateString(),
                reporterId,
                request.getReportedUserId(),
                request.getReason()
        );
        reportRepository.save(report);
        return toDto(report);
    }

    public List<ModerationReportDto> getOpenReports() {
        return reportRepository.findByStatusOrderByCreatedAtDesc("OPEN")
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public void banUser(String adminId, BanUserRequest request) {
        AuditLogEntity audit = new AuditLogEntity(
                UUIDv7Utils.generateString(),
                adminId,
                "BAN_USER",
                request.getTargetUserId(),
                request.getReason()
        );
        auditLogRepository.save(audit);

        // Outbox event for user ban
        try {
            String payloadJson = objectMapper.writeValueAsString(Map.of(
                    "targetUserId", request.getTargetUserId(),
                    "adminId", adminId,
                    "reason", request.getReason()
            ));
            OutboxEventEntity outbox = new OutboxEventEntity(
                    UUIDv7Utils.generateString(),
                    "USER",
                    request.getTargetUserId(),
                    EventTopics.ADMIN_USER_BANNED,
                    payloadJson
            );
            outboxEventRepository.save(outbox);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize outbox event payload", e);
        }
    }

    public List<AuditLogDto> getAuditLogs() {
        return auditLogRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(a -> new AuditLogDto(a.getId(), a.getAdminId(), a.getAction(), a.getTargetId(), a.getDetails(), a.getCreatedAt().toString()))
                .toList();
    }

    private ModerationReportDto toDto(ModerationReportEntity report) {
        return new ModerationReportDto(
                report.getId(),
                report.getReporterId(),
                report.getReportedUserId(),
                report.getReason(),
                report.getStatus(),
                report.getCreatedAt().toString()
        );
    }
}
