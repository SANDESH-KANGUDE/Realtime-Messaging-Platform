package com.company.chatplatform.adminservice.domain.repository;

import com.company.chatplatform.adminservice.domain.entity.AuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLogEntity, String> {
    List<AuditLogEntity> findAllByOrderByCreatedAtDesc();
}
