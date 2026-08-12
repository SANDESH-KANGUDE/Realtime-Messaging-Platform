package com.company.chatplatform.searchservice.domain.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "search_index")
public class SearchIndexDocument {

    @Id
    private String id;
    private String entityId;
    private String entityType; // MESSAGE, USER, CHAT

    @TextIndexed(weight = 5)
    private String title;

    @TextIndexed(weight = 3)
    private String content;

    private String metadata;
    private Instant createdAt = Instant.now();

    public SearchIndexDocument() {}

    public SearchIndexDocument(String id, String entityId, String entityType, String title, String content, String metadata) {
        this.id = id;
        this.entityId = entityId;
        this.entityType = entityType;
        this.title = title;
        this.content = content;
        this.metadata = metadata;
        this.createdAt = Instant.now();
    }

    public String getId() {
        return id;
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

    public String getContent() {
        return content;
    }

    public String getMetadata() {
        return metadata;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
