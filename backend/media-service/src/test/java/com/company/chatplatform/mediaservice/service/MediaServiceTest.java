package com.company.chatplatform.mediaservice.service;

import com.company.chatplatform.mediaservice.domain.entity.MediaMetadataEntity;
import com.company.chatplatform.mediaservice.domain.repository.MediaMetadataRepository;
import com.company.chatplatform.mediaservice.domain.repository.OutboxEventRepository;
import com.company.chatplatform.mediaservice.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

class MediaServiceTest {

    private MediaMetadataRepository mediaMetadataRepository;
    private OutboxEventRepository outboxEventRepository;
    private ObjectMapper objectMapper;
    private MediaService mediaService;

    @BeforeEach
    void setUp() {
        mediaMetadataRepository = Mockito.mock(MediaMetadataRepository.class);
        outboxEventRepository = Mockito.mock(OutboxEventRepository.class);
        objectMapper = new ObjectMapper();

        mediaService = new MediaService(mediaMetadataRepository, outboxEventRepository, objectMapper);
    }

    @Test
    void generateUploadUrl_Success() {
        UploadUrlRequest req = new UploadUrlRequest("image.jpg", "image/jpeg", 1024);

        UploadUrlResponse res = mediaService.generateUploadUrl("user-1", req);

        assertNotNull(res);
        assertNotNull(res.getMediaId());
        assertNotNull(res.getUploadUrl());
        Mockito.verify(mediaMetadataRepository, Mockito.times(1)).save(any(MediaMetadataEntity.class));
    }

    @Test
    void confirmUpload_Success() {
        MediaMetadataEntity entity = new MediaMetadataEntity("media-1", "user-1", "doc.pdf", "application/pdf", 2048, "key/1", "http://url/1");
        Mockito.when(mediaMetadataRepository.findById("media-1")).thenReturn(Optional.of(entity));

        MediaMetadataDto dto = mediaService.confirmUpload("user-1", new ConfirmUploadRequest("media-1"));

        assertNotNull(dto);
        assertEquals("UPLOADED", dto.getStatus());
        Mockito.verify(outboxEventRepository, Mockito.times(1)).save(any());
    }
}
