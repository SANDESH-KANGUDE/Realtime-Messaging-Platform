package com.company.chatplatform.searchservice.service;

import com.company.chatplatform.searchservice.dto.SearchResultDto;
import com.company.chatplatform.searchservice.provider.SearchProvider;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SearchService {

    private final SearchProvider searchProvider;

    public SearchService(SearchProvider searchProvider) {
        this.searchProvider = searchProvider;
    }

    public List<SearchResultDto> search(String query, String type) {
        return searchProvider.search(query, type);
    }

    public void index(String entityId, String entityType, String title, String content, String metadata) {
        searchProvider.indexEntity(entityId, entityType, title, content, metadata);
    }
}
