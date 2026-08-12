package com.company.chatplatform.userservice.service;

import com.company.chatplatform.common.core.exception.ResourceNotFoundException;
import com.company.chatplatform.common.core.util.UUIDv7Utils;
import com.company.chatplatform.common.kafka.topics.EventTopics;
import com.company.chatplatform.userservice.domain.entity.OutboxEventEntity;
import com.company.chatplatform.userservice.domain.entity.UserProfile;
import com.company.chatplatform.userservice.domain.entity.UserPreference;
import com.company.chatplatform.userservice.domain.repository.OutboxEventRepository;
import com.company.chatplatform.userservice.domain.repository.UserProfileRepository;
import com.company.chatplatform.userservice.domain.repository.UserPreferenceRepository;
import com.company.chatplatform.userservice.dto.UpdateProfileRequest;
import com.company.chatplatform.userservice.dto.UserProfileDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class UserService {

    private final UserProfileRepository userProfileRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public UserService(
            UserProfileRepository userProfileRepository,
            UserPreferenceRepository userPreferenceRepository,
            OutboxEventRepository outboxEventRepository,
            ObjectMapper objectMapper
    ) {
        this.userProfileRepository = userProfileRepository;
        this.userPreferenceRepository = userPreferenceRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public UserProfileDto createProfileFromRegistration(String userId, String email, String username, String displayName, String phoneNumber) {
        UserProfile profile = new UserProfile(userId, username, displayName, email);
        profile.setPhoneNumber(phoneNumber);
        userProfileRepository.save(profile);

        UserPreference preference = new UserPreference(userId);
        userPreferenceRepository.save(preference);

        return toDto(profile);
    }

    @Transactional
    public UserProfileDto createProfileFromRegistration(String userId, String email, String username, String displayName) {
        return createProfileFromRegistration(userId, email, username, displayName, null);
    }

    public UserProfileDto getProfile(String userId) {
        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User profile not found", "USER_PROFILE_NOT_FOUND"));
        return toDto(profile);
    }

    @Transactional
    public UserProfileDto updateProfile(String userId, UpdateProfileRequest request) {
        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User profile not found", "USER_PROFILE_NOT_FOUND"));

        if (request.getDisplayName() != null && !request.getDisplayName().isBlank()) {
            profile.setDisplayName(request.getDisplayName());
        }
        if (request.getAvatarUrl() != null) {
            profile.setAvatarUrl(request.getAvatarUrl());
        }
        if (request.getBio() != null) {
            profile.setBio(request.getBio());
        }
        if (request.getStatusMessage() != null) {
            profile.setStatusMessage(request.getStatusMessage());
        }
        profile.setUpdatedAt(Instant.now());
        userProfileRepository.save(profile);

        // Outbox event for user profile update
        try {
            String payloadJson = objectMapper.writeValueAsString(Map.of(
                    "userId", userId,
                    "displayName", profile.getDisplayName(),
                    "avatarUrl", profile.getAvatarUrl() != null ? profile.getAvatarUrl() : ""
            ));
            OutboxEventEntity outbox = new OutboxEventEntity(
                    UUIDv7Utils.generateString(),
                    "USER",
                    userId,
                    EventTopics.USER_PROFILE_UPDATED,
                    payloadJson
            );
            outboxEventRepository.save(outbox);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize outbox payload", e);
        }

        return toDto(profile);
    }

    public List<UserProfileDto> searchUsers(String query) {
        return userProfileRepository.findByUsernameContainingIgnoreCaseOrDisplayNameContainingIgnoreCase(query, query)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public UserProfileDto toDto(UserProfile profile) {
        return new UserProfileDto(
                profile.getUserId(),
                profile.getUsername(),
                profile.getDisplayName(),
                profile.getEmail(),
                profile.getPhoneNumber(),
                profile.getAvatarUrl(),
                profile.getBio(),
                profile.getStatusMessage(),
                profile.getCreatedAt().toString()
        );
    }
}
