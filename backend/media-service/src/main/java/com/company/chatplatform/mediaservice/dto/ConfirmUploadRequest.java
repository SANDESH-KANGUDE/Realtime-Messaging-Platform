package com.company.chatplatform.mediaservice.dto;

import jakarta.validation.constraints.NotBlank;

public class ConfirmUploadRequest {

    @NotBlank(message = "Media ID is required")
    private String mediaId;

    public ConfirmUploadRequest() {}

    public ConfirmUploadRequest(String mediaId) {
        this.mediaId = mediaId;
    }

    public String getMediaId() {
        return mediaId;
    }

    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }
}
