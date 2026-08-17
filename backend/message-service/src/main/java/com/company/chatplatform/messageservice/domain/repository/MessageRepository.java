package com.company.chatplatform.messageservice.domain.repository;

import com.company.chatplatform.messageservice.domain.document.MessageDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends MongoRepository<MessageDocument, String> {
    Page<MessageDocument> findByChatIdOrderByCreatedAtDesc(String chatId, Pageable pageable);
    
    java.util.List<MessageDocument> findByChatId(String chatId);

    @org.springframework.data.mongodb.repository.Query("{ 'chatId': ?1, 'senderId': { $ne: ?0 }, 'readReceipts.userId': { $ne: ?0 } }")
    java.util.List<MessageDocument> findUnreadMessagesForChat(String userId, String chatId);

    @org.springframework.data.mongodb.repository.Query("{ 'senderId': { $ne: ?0 }, 'readReceipts.userId': { $ne: ?0 } }")
    java.util.List<MessageDocument> findUnreadMessagesForUser(String userId);
}
