package com.company.chatplatform.mediaservice.dto;

public class UploadUrlResponse {
    private String mediaId;
    private String uploadUrl;
    private String objectKey;

    public UploadUrlResponse() {}

    public UploadUrlResponse(String mediaId, String uploadUrl, String objectKey) {
        this.mediaId = mediaId;
        this.uploadUrl = uploadUrl;
        this.objectKey = objectKey;
    }

    public String getMediaId() {
        return mediaId;
    }

    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }

    public String getUploadUrl() {
        return uploadUrl;
    }

    public void setUploadUrl(String uploadUrl) {
        this.uploadUrl = uploadUrl;
    }

    public String getObjectKey() {
        return objectKey;
    }

    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }
}
