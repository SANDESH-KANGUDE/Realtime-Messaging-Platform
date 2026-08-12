package com.company.chatplatform.searchservice.controller;

import com.company.chatplatform.common.core.dto.ApiResponse;
import com.company.chatplatform.searchservice.dto.SearchResultDto;
import com.company.chatplatform.searchservice.service.SearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SearchResultDto>>> search(
            @RequestParam("q") String query,
            @RequestParam(value = "type", defaultValue = "ALL") String type
    ) {
        List<SearchResultDto> results = searchService.search(query, type);
        return ResponseEntity.ok(ApiResponse.success(results));
    }
}
