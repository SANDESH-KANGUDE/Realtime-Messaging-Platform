package com.company.chatplatform.userservice.domain.repository;

import com.company.chatplatform.userservice.domain.entity.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, String> {
    
    @Query("SELECT f FROM Friendship f WHERE (f.requesterId = :user1 AND f.addresseeId = :user2) OR (f.requesterId = :user2 AND f.addresseeId = :user1)")
    Optional<Friendship> findFriendshipBetween(@Param("user1") String user1, @Param("user2") String user2);

    @Query("SELECT f FROM Friendship f WHERE (f.requesterId = :userId OR f.addresseeId = :userId) AND f.status = :status")
    List<Friendship> findAllByUserIdAndStatus(@Param("userId") String userId, @Param("status") String status);

    List<Friendship> findByAddresseeIdAndStatus(String addresseeId, String status);
}
