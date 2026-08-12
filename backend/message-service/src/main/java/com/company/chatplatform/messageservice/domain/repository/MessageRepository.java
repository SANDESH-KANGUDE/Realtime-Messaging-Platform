package com.company.chatplatform.messageservice.domain.repository;

import com.company.chatplatform.messageservice.domain.document.MessageDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends MongoRepository<MessageDocument, String> {
    Page<MessageDocument> findByChatIdOrderByCreatedAtDesc(String chatId, Pageable pageable);
}
