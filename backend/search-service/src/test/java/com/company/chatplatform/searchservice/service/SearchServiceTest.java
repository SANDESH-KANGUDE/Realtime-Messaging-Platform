package com.company.chatplatform.searchservice.service;

import com.company.chatplatform.searchservice.dto.SearchResultDto;
import com.company.chatplatform.searchservice.provider.SearchProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SearchServiceTest {

    private SearchProvider searchProvider;
    private SearchService searchService;

    @BeforeEach
    void setUp() {
        searchProvider = Mockito.mock(SearchProvider.class);
        searchService = new SearchService(searchProvider);
    }

    @Test
    void search_Success() {
        SearchResultDto res = new SearchResultDto("msg-1", "MESSAGE", "Message", "Hello", "2026-07-21T00:00:00Z");
        Mockito.when(searchProvider.search("Hello", "ALL")).thenReturn(List.of(res));

        List<SearchResultDto> results = searchService.search("Hello", "ALL");

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("msg-1", results.get(0).getEntityId());
    }
}
