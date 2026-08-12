package com.company.chatplatform.adminservice.service;

import com.company.chatplatform.adminservice.domain.entity.ModerationReportEntity;
import com.company.chatplatform.adminservice.domain.repository.AuditLogRepository;
import com.company.chatplatform.adminservice.domain.repository.ModerationReportRepository;
import com.company.chatplatform.adminservice.domain.repository.OutboxEventRepository;
import com.company.chatplatform.adminservice.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

class AdminServiceTest {

    private ModerationReportRepository reportRepository;
    private AuditLogRepository auditLogRepository;
    private OutboxEventRepository outboxEventRepository;
    private ObjectMapper objectMapper;
    private AdminService adminService;

    @BeforeEach
    void setUp() {
        reportRepository = Mockito.mock(ModerationReportRepository.class);
        auditLogRepository = Mockito.mock(AuditLogRepository.class);
        outboxEventRepository = Mockito.mock(OutboxEventRepository.class);
        objectMapper = new ObjectMapper();

        adminService = new AdminService(reportRepository, auditLogRepository, outboxEventRepository, objectMapper);
    }

    @Test
    void createReport_Success() {
        ReportUserRequest req = new ReportUserRequest("bad-user", "Spam messages");

        ModerationReportDto report = adminService.createReport("reporter-1", req);

        assertNotNull(report);
        assertEquals("bad-user", report.getReportedUserId());
        assertEquals("OPEN", report.getStatus());
        Mockito.verify(reportRepository, Mockito.times(1)).save(any(ModerationReportEntity.class));
    }

    @Test
    void banUser_Success() {
        BanUserRequest req = new BanUserRequest("bad-user", "Repeated violations");

        adminService.banUser("admin-1", req);

        Mockito.verify(auditLogRepository, Mockito.times(1)).save(any());
        Mockito.verify(outboxEventRepository, Mockito.times(1)).save(any());
    }
}
