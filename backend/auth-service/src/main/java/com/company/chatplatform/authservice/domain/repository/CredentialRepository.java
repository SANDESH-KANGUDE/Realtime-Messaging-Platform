package com.company.chatplatform.authservice.domain.repository;

import com.company.chatplatform.authservice.domain.entity.Credential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CredentialRepository extends JpaRepository<Credential, String> {
    Optional<Credential> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<Credential> findByPhoneNumber(String phoneNumber);
    boolean existsByPhoneNumber(String phoneNumber);
}
