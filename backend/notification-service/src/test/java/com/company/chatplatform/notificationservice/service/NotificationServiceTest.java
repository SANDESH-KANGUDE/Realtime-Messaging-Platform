package com.company.chatplatform.notificationservice.service;

import com.company.chatplatform.notificationservice.domain.entity.NotificationEntity;
import com.company.chatplatform.notificationservice.domain.repository.NotificationRepository;
import com.company.chatplatform.notificationservice.dto.NotificationDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

class NotificationServiceTest {

    private NotificationRepository notificationRepository;
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationRepository = Mockito.mock(NotificationRepository.class);
        notificationService = new NotificationService(notificationRepository);
    }

    @Test
    void createNotification_Success() {
        NotificationDto notification = notificationService.createNotification("user-1", "Welcome", "Welcome to Chat!", "SYSTEM");

        assertNotNull(notification);
        assertEquals("user-1", notification.getRecipientId());
        assertEquals("Welcome", notification.getTitle());
        Mockito.verify(notificationRepository, Mockito.times(1)).save(any(NotificationEntity.class));
    }

    @Test
    void getUserNotifications_Success() {
        NotificationEntity n = new NotificationEntity("n-1", "user-1", "Hi", "Body", "SYSTEM");
        Mockito.when(notificationRepository.findByRecipientIdOrderByCreatedAtDesc("user-1")).thenReturn(List.of(n));

        List<NotificationDto> list = notificationService.getUserNotifications("user-1");

        assertEquals(1, list.size());
        assertEquals("Hi", list.get(0).getTitle());
    }
}
