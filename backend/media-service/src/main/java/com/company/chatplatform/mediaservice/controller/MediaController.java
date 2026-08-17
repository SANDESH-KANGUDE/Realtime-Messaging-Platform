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

    @PutMapping("/mock-storage/uploads/{uploaderId}/{fileName}")
    public ResponseEntity<Void> uploadMockFile(
            @PathVariable("uploaderId") String uploaderId,
            @PathVariable("fileName") String fileName,
            @RequestBody byte[] content
    ) throws Exception {
        java.nio.file.Path dir = java.nio.file.Paths.get("uploads", uploaderId);
        java.nio.file.Files.createDirectories(dir);
        java.nio.file.Path file = dir.resolve(fileName);
        java.nio.file.Files.write(file, content);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/files/uploads/{uploaderId}/{fileName}")
    public ResponseEntity<byte[]> getMockFile(
            @PathVariable("uploaderId") String uploaderId,
            @PathVariable("fileName") String fileName
    ) throws Exception {
        java.nio.file.Path file = java.nio.file.Paths.get("uploads", uploaderId, fileName);
        if (!java.nio.file.Files.exists(file)) {
            return ResponseEntity.notFound().build();
        }
        byte[] content = java.nio.file.Files.readAllBytes(file);
        String contentType = java.nio.file.Files.probeContentType(file);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_TYPE, contentType)
                .body(content);
    }
}
