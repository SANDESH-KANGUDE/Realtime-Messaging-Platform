package com.company.chatplatform.messageservice.domain.repository;

import com.company.chatplatform.messageservice.domain.document.OutboxMessageDocument;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutboxMessageRepository extends MongoRepository<OutboxMessageDocument, String> {
    List<OutboxMessageDocument> findByStatusOrderByCreatedAtAsc(String status, Pageable pageable);
}
