package com.company.chatplatform.realtimeservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;

class PresenceServiceTest {

    private StringRedisTemplate redisTemplate;
    private ValueOperations<String, String> valueOperations;
    private SetOperations<String, String> setOperations;
    private PresenceService presenceService;

    @BeforeEach
    void setUp() {
        redisTemplate = Mockito.mock(StringRedisTemplate.class);
        valueOperations = Mockito.mock(ValueOperations.class);
        setOperations = Mockito.mock(SetOperations.class);

        Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        Mockito.when(redisTemplate.opsForSet()).thenReturn(setOperations);

        presenceService = new PresenceService(redisTemplate);
    }

    @Test
    void registerConnect_Success() {
        presenceService.registerConnect("user-1", "socket-123");

        Mockito.verify(valueOperations, Mockito.times(1)).set("socket:socket-123", "user-1");
        Mockito.verify(setOperations, Mockito.times(1)).add("connections:user-1", "socket-123");
        Mockito.verify(valueOperations, Mockito.times(1)).set("presence:user-1", "ONLINE");
    }

    @Test
    void getUserStatus_Online() {
        Mockito.when(valueOperations.get("presence:user-1")).thenReturn("ONLINE");

        String status = presenceService.getUserStatus("user-1");

        assertEquals("ONLINE", status);
        assertTrue(presenceService.isUserOnline("user-1"));
    }
}
