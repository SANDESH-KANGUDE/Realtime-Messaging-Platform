package com.company.chatplatform.authservice.service;

import com.company.chatplatform.authservice.domain.entity.Credential;
import com.company.chatplatform.authservice.domain.entity.OutboxEventEntity;
import com.company.chatplatform.authservice.domain.entity.RefreshTokenEntity;
import com.company.chatplatform.authservice.domain.repository.CredentialRepository;
import com.company.chatplatform.authservice.domain.repository.OutboxEventRepository;
import com.company.chatplatform.authservice.domain.repository.RefreshTokenRepository;
import com.company.chatplatform.authservice.dto.*;
import com.company.chatplatform.common.core.exception.ConflictException;
import com.company.chatplatform.common.core.exception.ResourceNotFoundException;
import com.company.chatplatform.common.core.exception.UnauthorizedException;
import com.company.chatplatform.common.core.util.UUIDv7Utils;
import com.company.chatplatform.common.kafka.topics.EventTopics;
import com.company.chatplatform.common.security.jwt.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;

@Service
public class AuthService {

    private final CredentialRepository credentialRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;

    public AuthService(
            CredentialRepository credentialRepository,
            RefreshTokenRepository refreshTokenRepository,
            OutboxEventRepository outboxEventRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            ObjectMapper objectMapper
    ) {
        this.credentialRepository = credentialRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (credentialRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email address already registered", "EMAIL_ALREADY_EXISTS");
        }
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank() && credentialRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new ConflictException("Phone number already registered", "PHONE_ALREADY_EXISTS");
        }

        String userId = UUIDv7Utils.generateString();
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        Credential credential = new Credential(userId, request.getEmail(), encodedPassword, "ROLE_USER", request.getPhoneNumber());
        credentialRepository.save(credential);

        // Transactional Outbox Event for User Registration
        try {
            Map<String, String> eventPayload = new java.util.HashMap<>(Map.of(
                    "userId", userId,
                    "email", request.getEmail(),
                    "username", request.getUsername(),
                    "displayName", request.getDisplayName() != null ? request.getDisplayName() : request.getUsername()
            ));
            if (request.getPhoneNumber() != null) {
                eventPayload.put("phoneNumber", request.getPhoneNumber());
            }
            String payloadJson = objectMapper.writeValueAsString(eventPayload);
            OutboxEventEntity outboxEvent = new OutboxEventEntity(
                    UUIDv7Utils.generateString(),
                    "USER",
                    userId,
                    EventTopics.AUTH_USER_REGISTERED,
                    payloadJson
            );
            outboxEventRepository.save(outboxEvent);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize outbox event payload", e);
        }

        String accessToken = jwtTokenProvider.generateAccessToken(userId, credential.getEmail(), credential.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(userId);
        saveRefreshToken(userId, refreshToken);

        return new AuthResponse(accessToken, refreshToken, userId, credential.getEmail(), credential.getRole());
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        Credential credential;
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()) {
            credential = credentialRepository.findByPhoneNumber(request.getPhoneNumber())
                    .orElseThrow(() -> new UnauthorizedException("Invalid email/phone or password", "INVALID_CREDENTIALS"));
        } else {
            credential = credentialRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new UnauthorizedException("Invalid email/phone or password", "INVALID_CREDENTIALS"));
        }

        if (!passwordEncoder.matches(request.getPassword(), credential.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email/phone or password", "INVALID_CREDENTIALS");
        }

        String accessToken = jwtTokenProvider.generateAccessToken(credential.getUserId(), credential.getEmail(), credential.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(credential.getUserId());
        saveRefreshToken(credential.getUserId(), refreshToken);

        return new AuthResponse(accessToken, refreshToken, credential.getUserId(), credential.getEmail(), credential.getRole());
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        if (!jwtTokenProvider.validateToken(request.getRefreshToken())) {
            throw new UnauthorizedException("Invalid or expired refresh token", "INVALID_REFRESH_TOKEN");
        }

        RefreshTokenEntity tokenEntity = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new UnauthorizedException("Refresh token not found", "REFRESH_TOKEN_NOT_FOUND"));

        if (tokenEntity.isRevoked() || tokenEntity.getExpiresAt().isBefore(Instant.now())) {
            throw new UnauthorizedException("Refresh token has been revoked or expired", "EXPIRED_REFRESH_TOKEN");
        }

        Credential credential = credentialRepository.findById(tokenEntity.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User credential not found", "USER_NOT_FOUND"));

        String newAccessToken = jwtTokenProvider.generateAccessToken(credential.getUserId(), credential.getEmail(), credential.getRole());
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(credential.getUserId());

        tokenEntity.setRevoked(true);
        refreshTokenRepository.save(tokenEntity);
        saveRefreshToken(credential.getUserId(), newRefreshToken);

        return new AuthResponse(newAccessToken, newRefreshToken, credential.getUserId(), credential.getEmail(), credential.getRole());
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
    }

    public UserMeResponse getCurrentUser(String userId) {
        Credential credential = credentialRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found", "USER_NOT_FOUND"));

        return new UserMeResponse(credential.getUserId(), credential.getEmail(), credential.getRole(), credential.getCreatedAt().toString());
    }

    private void saveRefreshToken(String userId, String token) {
        Instant expiresAt = Instant.now().plusMillis(604800000); // 7 days
        RefreshTokenEntity entity = new RefreshTokenEntity(UUIDv7Utils.generateString(), userId, token, expiresAt);
        refreshTokenRepository.save(entity);
    }
}
