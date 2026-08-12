package com.company.chatplatform.adminservice.domain.repository;

import com.company.chatplatform.adminservice.domain.entity.ModerationReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModerationReportRepository extends JpaRepository<ModerationReportEntity, String> {
    List<ModerationReportEntity> findByStatusOrderByCreatedAtDesc(String status);
}
