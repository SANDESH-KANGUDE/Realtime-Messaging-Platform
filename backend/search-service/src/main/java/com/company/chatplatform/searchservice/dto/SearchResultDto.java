package com.company.chatplatform.searchservice.dto;

public class SearchResultDto {
    private String entityId;
    private String entityType;
    private String title;
    private String snippet;
    private String metadata;
    private String createdAt;

    public SearchResultDto() {}

    public SearchResultDto(String entityId, String entityType, String title, String snippet, String metadata, String createdAt) {
        this.entityId = entityId;
        this.entityType = entityType;
        this.title = title;
        this.snippet = snippet;
        this.metadata = metadata;
        this.createdAt = createdAt;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
