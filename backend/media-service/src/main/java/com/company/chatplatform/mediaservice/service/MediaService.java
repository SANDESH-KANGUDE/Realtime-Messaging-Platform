package com.company.chatplatform.mediaservice.service;

import com.company.chatplatform.common.core.exception.ResourceNotFoundException;
import com.company.chatplatform.common.core.exception.BadRequestException;
import com.company.chatplatform.common.core.util.UUIDv7Utils;
import com.company.chatplatform.common.kafka.topics.EventTopics;
import com.company.chatplatform.mediaservice.domain.entity.MediaMetadataEntity;
import com.company.chatplatform.mediaservice.domain.entity.OutboxEventEntity;
import com.company.chatplatform.mediaservice.domain.repository.MediaMetadataRepository;
import com.company.chatplatform.mediaservice.domain.repository.OutboxEventRepository;
import com.company.chatplatform.mediaservice.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;

@Service
public class MediaService {

    private static final java.util.Set<String> ALLOWED_MIME_TYPES = java.util.Set.of(
        "image/png", "image/jpeg", "image/jpg", "image/gif", "image/webp",
        "application/pdf", "text/plain", "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "audio/mpeg", "audio/wav", "audio/ogg", "audio/aac", "audio/mp4", "audio/webm",
        "video/mp4", "video/webm", "video/ogg", "video/mpeg"
    );

    private final MediaMetadataRepository mediaMetadataRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public MediaService(MediaMetadataRepository mediaMetadataRepository, OutboxEventRepository outboxEventRepository, ObjectMapper objectMapper) {
        this.mediaMetadataRepository = mediaMetadataRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public UploadUrlResponse generateUploadUrl(String uploaderId, UploadUrlRequest request) {
        String fileType = request.getFileType();
        if (fileType == null || !ALLOWED_MIME_TYPES.contains(fileType.toLowerCase())) {
            throw new BadRequestException("Invalid or disallowed file type: " + fileType, "INVALID_FILE_TYPE");
        }

        String mediaId = UUIDv7Utils.generateString();
        String objectKey = "uploads/" + uploaderId + "/" + mediaId + "_" + request.getFileName();
        String mockUploadUrl = "http://localhost:8087/api/v1/media/mock-storage/" + objectKey;
        String publicUrl = "http://localhost:8087/api/v1/media/files/" + objectKey;

        MediaMetadataEntity entity = new MediaMetadataEntity(
                mediaId,
                uploaderId,
                request.getFileName(),
                request.getFileType(),
                request.getFileSize(),
                objectKey,
                publicUrl
        );
        mediaMetadataRepository.save(entity);

        return new UploadUrlResponse(mediaId, mockUploadUrl, objectKey);
    }

    @Transactional
    public MediaMetadataDto confirmUpload(String uploaderId, ConfirmUploadRequest request) {
        MediaMetadataEntity entity = mediaMetadataRepository.findById(request.getMediaId())
                .orElseThrow(() -> new ResourceNotFoundException("Media metadata not found", "MEDIA_NOT_FOUND"));

        entity.setStatus("UPLOADED");
        entity.setUpdatedAt(Instant.now());
        mediaMetadataRepository.save(entity);

        // Outbox Event for media upload
        try {
            String payloadJson = objectMapper.writeValueAsString(Map.of(
                    "mediaId", entity.getId(),
                    "uploaderId", uploaderId,
                    "url", entity.getUrl(),
                    "fileType", entity.getFileType(),
                    "fileSize", entity.getFileSize()
            ));
            OutboxEventEntity outbox = new OutboxEventEntity(
                    UUIDv7Utils.generateString(),
                    "MEDIA",
                    entity.getId(),
                    EventTopics.MEDIA_UPLOADED,
                    payloadJson
            );
            outboxEventRepository.save(outbox);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize outbox event", e);
        }

        return toDto(entity);
    }

    public MediaMetadataDto getMediaDetails(String mediaId) {
        MediaMetadataEntity entity = mediaMetadataRepository.findById(mediaId)
                .orElseThrow(() -> new ResourceNotFoundException("Media not found", "MEDIA_NOT_FOUND"));
        return toDto(entity);
    }

    private MediaMetadataDto toDto(MediaMetadataEntity entity) {
        return new MediaMetadataDto(
                entity.getId(),
                entity.getUploaderId(),
                entity.getFileName(),
                entity.getFileType(),
                entity.getFileSize(),
                entity.getUrl(),
                entity.getStatus(),
                entity.getCreatedAt().toString()
        );
    }
}
