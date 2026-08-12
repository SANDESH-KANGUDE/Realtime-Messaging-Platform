package com.company.chatplatform.authservice.domain.repository;

import com.company.chatplatform.authservice.domain.entity.OutboxEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEventEntity, String> {
    List<OutboxEventEntity> findTop50ByStatusOrderByCreatedAtAsc(String status);
}
