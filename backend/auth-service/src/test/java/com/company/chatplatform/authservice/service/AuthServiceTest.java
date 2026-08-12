package com.company.chatplatform.authservice.service;

import com.company.chatplatform.authservice.domain.entity.Credential;
import com.company.chatplatform.authservice.domain.repository.CredentialRepository;
import com.company.chatplatform.authservice.domain.repository.OutboxEventRepository;
import com.company.chatplatform.authservice.domain.repository.RefreshTokenRepository;
import com.company.chatplatform.authservice.dto.AuthResponse;
import com.company.chatplatform.authservice.dto.RegisterRequest;
import com.company.chatplatform.common.core.exception.ConflictException;
import com.company.chatplatform.common.security.jwt.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

class AuthServiceTest {

    private CredentialRepository credentialRepository;
    private RefreshTokenRepository refreshTokenRepository;
    private OutboxEventRepository outboxEventRepository;
    private PasswordEncoder passwordEncoder;
    private JwtTokenProvider jwtTokenProvider;
    private ObjectMapper objectMapper;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        credentialRepository = Mockito.mock(CredentialRepository.class);
        refreshTokenRepository = Mockito.mock(RefreshTokenRepository.class);
        outboxEventRepository = Mockito.mock(OutboxEventRepository.class);
        passwordEncoder = Mockito.mock(PasswordEncoder.class);
        jwtTokenProvider = new JwtTokenProvider("testSecretKeyThatIsMinimum256BitsLongForTestingPurposes!", 900000, 604800000);
        objectMapper = new ObjectMapper();

        authService = new AuthService(
                credentialRepository,
                refreshTokenRepository,
                outboxEventRepository,
                passwordEncoder,
                jwtTokenProvider,
                objectMapper
        );
    }

    @Test
    void register_Success() {
        RegisterRequest request = new RegisterRequest("test@example.com", "password123", "testuser", "Test User");
        Mockito.when(credentialRepository.existsByEmail("test@example.com")).thenReturn(false);
        Mockito.when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("test@example.com", response.getEmail());
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        Mockito.verify(credentialRepository, Mockito.times(1)).save(any());
        Mockito.verify(outboxEventRepository, Mockito.times(1)).save(any());
    }

    @Test
    void register_DuplicateEmail_ThrowsConflictException() {
        RegisterRequest request = new RegisterRequest("existing@example.com", "password123", "existinguser", "Existing User");
        Mockito.when(credentialRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThrows(ConflictException.class, () -> authService.register(request));
    }
}
