package com.company.chatplatform.userservice.service;

import com.company.chatplatform.userservice.domain.entity.UserProfile;
import com.company.chatplatform.userservice.domain.repository.OutboxEventRepository;
import com.company.chatplatform.userservice.domain.repository.UserProfileRepository;
import com.company.chatplatform.userservice.domain.repository.UserPreferenceRepository;
import com.company.chatplatform.userservice.dto.UserProfileDto;
import com.company.chatplatform.userservice.domain.repository.BlockedUserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

class UserServiceTest {

    private UserProfileRepository userProfileRepository;
    private UserPreferenceRepository userPreferenceRepository;
    private OutboxEventRepository outboxEventRepository;
    private BlockedUserRepository blockedUserRepository;
    private ObjectMapper objectMapper;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userProfileRepository = Mockito.mock(UserProfileRepository.class);
        userPreferenceRepository = Mockito.mock(UserPreferenceRepository.class);
        outboxEventRepository = Mockito.mock(OutboxEventRepository.class);
        blockedUserRepository = Mockito.mock(BlockedUserRepository.class);
        objectMapper = new ObjectMapper();

        userService = new UserService(
                userProfileRepository,
                userPreferenceRepository,
                outboxEventRepository,
                blockedUserRepository,
                objectMapper
        );
    }

    @Test
    void createProfileFromRegistration_Success() {
        UserProfileDto dto = userService.createProfileFromRegistration("user-1", "user1@example.com", "user1", "User One");

        assertNotNull(dto);
        assertEquals("user-1", dto.getUserId());
        assertEquals("user1@example.com", dto.getEmail());
        assertEquals("user1", dto.getUsername());
        Mockito.verify(userProfileRepository, Mockito.times(1)).save(any());
        Mockito.verify(userPreferenceRepository, Mockito.times(1)).save(any());
    }

    @Test
    void getProfile_Success() {
        UserProfile profile = new UserProfile("user-1", "user1", "User One", "user1@example.com");
        Mockito.when(userProfileRepository.findById("user-1")).thenReturn(Optional.of(profile));

        UserProfileDto result = userService.getProfile("user-1");

        assertNotNull(result);
        assertEquals("User One", result.getDisplayName());
    }
}
