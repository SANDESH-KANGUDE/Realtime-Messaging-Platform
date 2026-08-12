package com.company.chatplatform.searchservice.dto;

public class SearchResultDto {
    private String entityId;
    private String entityType;
    private String title;
    private String snippet;
    private String createdAt;

    public SearchResultDto() {}

    public SearchResultDto(String entityId, String entityType, String title, String snippet, String createdAt) {
        this.entityId = entityId;
        this.entityType = entityType;
        this.title = title;
        this.snippet = snippet;
        this.createdAt = createdAt;
    }

    public String getEntityId() {
        return entityId;
    }

    public String getEntityType() {
        return entityType;
    }

    public String getTitle() {
        return title;
    }

    public String getSnippet() {
        return snippet;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
