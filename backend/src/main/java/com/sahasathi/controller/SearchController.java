package com.sahasathi.controller;

import com.sahasathi.dto.ApiResponse;
import com.sahasathi.dto.SearchResult;
import com.sahasathi.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<SearchResult>>> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String locality,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("Search query: q={}, type={}, city={}, locality={}", q, type, city, locality);

        Page<SearchResult> results = searchService.search(q, type, city, locality, page, size);

        return ResponseEntity.ok(ApiResponse.success(results, "Search results fetched successfully"));
    }
}
