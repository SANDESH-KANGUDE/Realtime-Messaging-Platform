package com.company.chatplatform.searchservice.provider;

import com.company.chatplatform.searchservice.dto.SearchResultDto;
import java.util.List;

public interface SearchProvider {
    void indexEntity(String entityId, String entityType, String title, String content, String metadata);
    List<SearchResultDto> search(String query, String entityType);
}
