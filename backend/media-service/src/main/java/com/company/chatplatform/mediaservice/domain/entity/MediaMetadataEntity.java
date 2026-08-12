package com.company.chatplatform.mediaservice.domain.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "media_metadata")
public class MediaMetadataEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "uploader_id", nullable = false, length = 36)
    private String uploaderId;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_type", nullable = false)
    private String fileType;

    @Column(name = "file_size", nullable = false)
    private long fileSize;

    @Column(name = "object_key", nullable = false, unique = true)
    private String objectKey;

    @Column(name = "url", nullable = false)
    private String url;

    @Column(name = "status", nullable = false)
    private String status = "PENDING"; // PENDING, UPLOADED, FAILED

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public MediaMetadataEntity() {}

    public MediaMetadataEntity(String id, String uploaderId, String fileName, String fileType, long fileSize, String objectKey, String url) {
        this.id = id;
        this.uploaderId = uploaderId;
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.objectKey = objectKey;
        this.url = url;
        this.status = "PENDING";
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public String getUploaderId() {
        return uploaderId;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public long getFileSize() {
        return fileSize;
    }

    public String getObjectKey() {
        return objectKey;
    }

    public String getUrl() {
        return url;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
