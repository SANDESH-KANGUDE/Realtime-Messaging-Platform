package com.company.chatplatform.userservice.domain.repository;

import com.company.chatplatform.userservice.domain.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, String> {
    Optional<UserProfile> findByUsername(String username);
    Optional<UserProfile> findByEmail(String email);
    List<UserProfile> findByUsernameContainingIgnoreCaseOrDisplayNameContainingIgnoreCase(String query1, String query2);
}
