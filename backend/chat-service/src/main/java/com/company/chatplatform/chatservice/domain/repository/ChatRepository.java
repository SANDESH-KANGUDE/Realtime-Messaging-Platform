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
        JOIN ChatMemberEntity m1 ON c.id = m1.chatId
        JOIN ChatMemberEntity m2 ON c.id = m2.chatId
        WHERE c.type = 'DIRECT' AND m1.userId = :user1 AND m2.userId = :user2
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
