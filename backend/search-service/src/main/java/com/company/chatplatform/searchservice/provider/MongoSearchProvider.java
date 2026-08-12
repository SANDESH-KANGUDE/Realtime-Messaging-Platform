package com.company.chatplatform.searchservice.provider;

import com.company.chatplatform.common.core.util.UUIDv7Utils;
import com.company.chatplatform.searchservice.domain.document.SearchIndexDocument;
import com.company.chatplatform.searchservice.domain.repository.SearchIndexRepository;
import com.company.chatplatform.searchservice.dto.SearchResultDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MongoSearchProvider implements SearchProvider {

    private final SearchIndexRepository searchIndexRepository;

    public MongoSearchProvider(SearchIndexRepository searchIndexRepository) {
        this.searchIndexRepository = searchIndexRepository;
    }

    @Override
    public void indexEntity(String entityId, String entityType, String title, String content, String metadata) {
        SearchIndexDocument doc = new SearchIndexDocument(
                UUIDv7Utils.generateString(),
                entityId,
                entityType,
                title,
                content,
                metadata
        );
        searchIndexRepository.save(doc);
    }

    @Override
    public List<SearchResultDto> search(String query, String entityType) {
        List<SearchIndexDocument> docs;
        if (entityType != null && !entityType.isBlank() && !"ALL".equalsIgnoreCase(entityType)) {
            docs = searchIndexRepository.searchByTextAndType(query, entityType.toUpperCase());
        } else {
            docs = searchIndexRepository.searchByText(query);
        }

        return docs.stream().map(d -> new SearchResultDto(
                d.getEntityId(),
                d.getEntityType(),
                d.getTitle(),
                d.getContent(),
                d.getCreatedAt().toString()
        )).toList();
    }
}
