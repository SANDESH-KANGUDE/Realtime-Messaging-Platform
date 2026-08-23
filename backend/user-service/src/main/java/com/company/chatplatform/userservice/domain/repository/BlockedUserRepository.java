package com.company.chatplatform.userservice.domain.repository;

import com.company.chatplatform.userservice.domain.entity.BlockedUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlockedUserRepository extends JpaRepository<BlockedUser, String> {
    Optional<BlockedUser> findByBlockerIdAndBlockedId(String blockerId, String blockedId);
    boolean existsByBlockerIdAndBlockedId(String blockerId, String blockedId);
    List<BlockedUser> findByBlockerId(String blockerId);
    void deleteByBlockerIdAndBlockedId(String blockerId, String blockedId);
}
