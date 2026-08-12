package com.company.chatplatform.mediaservice.controller;

import com.company.chatplatform.common.core.dto.ApiResponse;
import com.company.chatplatform.common.security.context.UserContextHolder;
import com.company.chatplatform.mediaservice.dto.*;
import com.company.chatplatform.mediaservice.service.MediaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/media")
public class MediaController {

    private final MediaService mediaService;

    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @PostMapping("/upload-url")
    public ResponseEntity<ApiResponse<UploadUrlResponse>> getUploadUrl(
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
            @Valid @RequestBody UploadUrlRequest request
    ) {
        String uploaderId = headerUserId != null ? headerUserId : UserContextHolder.getUserId();
        UploadUrlResponse response = mediaService.generateUploadUrl(uploaderId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response, "Upload URL generated"));
    }

    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<MediaMetadataDto>> confirmUpload(
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
            @Valid @RequestBody ConfirmUploadRequest request
    ) {
        String uploaderId = headerUserId != null ? headerUserId : UserContextHolder.getUserId();
        MediaMetadataDto metadata = mediaService.confirmUpload(uploaderId, request);
        return ResponseEntity.ok(ApiResponse.success(metadata, "Upload confirmed"));
    }

    @PostMapping("/{mediaId}/complete")
    public ResponseEntity<ApiResponse<MediaMetadataDto>> completeUpload(
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
            @PathVariable("mediaId") String mediaId
    ) {
        String uploaderId = headerUserId != null ? headerUserId : UserContextHolder.getUserId();
        MediaMetadataDto metadata = mediaService.confirmUpload(uploaderId, new ConfirmUploadRequest(mediaId));
        return ResponseEntity.ok(ApiResponse.success(metadata, "Upload confirmed"));
    }

    @GetMapping("/{mediaId}")
    public ResponseEntity<ApiResponse<MediaMetadataDto>> getMediaDetails(@PathVariable("mediaId") String mediaId) {
        MediaMetadataDto details = mediaService.getMediaDetails(mediaId);
        return ResponseEntity.ok(ApiResponse.success(details));
    }
}
