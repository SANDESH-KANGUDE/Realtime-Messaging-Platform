package com.company.chatplatform.mediaservice.dto;

public class MediaMetadataDto {
    private String id;
    private String uploaderId;
    private String fileName;
    private String fileType;
    private long fileSize;
    private String url;
    private String status;
    private String createdAt;

    public MediaMetadataDto() {}

    public MediaMetadataDto(String id, String uploaderId, String fileName, String fileType, long fileSize, String url, String status, String createdAt) {
        this.id = id;
        this.uploaderId = uploaderId;
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.url = url;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUploaderId() {
        return uploaderId;
    }

    public void setUploaderId(String uploaderId) {
        this.uploaderId = uploaderId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
