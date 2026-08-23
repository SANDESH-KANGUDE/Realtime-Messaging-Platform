package com.company.chatplatform.chatservice.domain.repository;

import com.company.chatplatform.chatservice.domain.entity.ChatEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRepository extends JpaRepository<ChatEntity, String> {

    @Query("""
        SELECT c FROM ChatEntity c
        WHERE c.type = 'DIRECT'
          AND c.id IN (SELECT m1.chatId FROM ChatMemberEntity m1 WHERE m1.userId = :user1)
          AND c.id IN (SELECT m2.chatId FROM ChatMemberEntity m2 WHERE m2.userId = :user2)
    """)
    Optional<ChatEntity> findDirectChatBetween(@Param("user1") String user1, @Param("user2") String user2);

    @Query("""
        SELECT c FROM ChatEntity c
        JOIN ChatMemberEntity m ON c.id = m.chatId
        WHERE m.userId = :userId
        ORDER BY c.updatedAt DESC
    """)
    List<ChatEntity> findAllChatsForUser(@Param("userId") String userId);
}
