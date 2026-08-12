package com.company.chatplatform.chatservice.domain.repository;

import com.company.chatplatform.chatservice.domain.entity.ChatMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatMemberRepository extends JpaRepository<ChatMemberEntity, String> {
    List<ChatMemberEntity> findByChatId(String chatId);
    Optional<ChatMemberEntity> findByChatIdAndUserId(String chatId, String userId);
    boolean existsByChatIdAndUserId(String chatId, String userId);
    void deleteByChatIdAndUserId(String chatId, String userId);
}
