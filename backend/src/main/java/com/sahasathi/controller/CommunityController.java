package com.sahasathi.controller;

import com.sahasathi.dto.ApiResponse;
import com.sahasathi.dto.CommunityListResponse;
import com.sahasathi.dto.CommunityResponse;
import com.sahasathi.dto.CreateCommunityRequest;
import com.sahasathi.dto.JoinResult;
import com.sahasathi.service.CommunityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/communities")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

    @PostMapping
    public ResponseEntity<ApiResponse<CommunityResponse>> createCommunity(
            @RequestParam Long userId,
            @Valid @RequestBody CreateCommunityRequest request) {
        log.info("Creating community for user: {}", userId);
        CommunityResponse response = communityService.createCommunity(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response, "Community created successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<CommunityListResponse>>> listCommunities(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String locality,
            @RequestParam Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Listing communities, city: {}", city);
        Page<CommunityListResponse> communities = communityService.listCommunities(city, locality, userId, page, size);
        return ResponseEntity.ok(ApiResponse.success(communities, "Communities fetched successfully"));
    }

    @GetMapping("/{communityId}")
    public ResponseEntity<ApiResponse<CommunityResponse>> getCommunity(
            @PathVariable Long communityId,
            @RequestParam Long userId) {
        log.info("Fetching community: {}", communityId);
        CommunityResponse response = communityService.getCommunity(communityId, userId);
        return ResponseEntity.ok(ApiResponse.success(response, "Community fetched successfully"));
    }

    @PutMapping("/{communityId}")
    public ResponseEntity<ApiResponse<CommunityResponse>> updateCommunity(
            @PathVariable Long communityId,
            @RequestParam Long userId,
            @Valid @RequestBody CreateCommunityRequest request) {
        log.info("Updating community: {} by user: {}", communityId, userId);
        CommunityResponse response = communityService.updateCommunity(communityId, userId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Community updated successfully"));
    }

    @DeleteMapping("/{communityId}")
    public ResponseEntity<ApiResponse<Void>> deleteCommunity(
            @PathVariable Long communityId,
            @RequestParam Long userId) {
        log.info("Deleting community: {} by user: {}", communityId, userId);
        communityService.deleteCommunity(communityId, userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Community deleted successfully"));
    }

    @PostMapping("/{communityId}/join")
    public ResponseEntity<ApiResponse<JoinResult>> joinCommunity(
            @PathVariable Long communityId,
            @RequestParam Long userId) {
        log.info("User {} joining community: {}", userId, communityId);
        JoinResult result = communityService.joinCommunity(communityId, userId);
        String message = "REQUEST_CREATED".equals(result.getType())
                ? "Join request sent for approval" : "Joined community successfully";
        return ResponseEntity.ok(ApiResponse.success(result, message));
    }

    @PostMapping("/{communityId}/leave")
    public ResponseEntity<ApiResponse<CommunityResponse>> leaveCommunity(
            @PathVariable Long communityId,
            @RequestParam Long userId) {
        log.info("User {} leaving community: {}", userId, communityId);
        CommunityResponse response = communityService.leaveCommunity(communityId, userId);
        return ResponseEntity.ok(ApiResponse.success(response, "Left community successfully"));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<CommunityListResponse>>> getUserCommunities(
            @PathVariable Long userId) {
        log.info("Fetching communities for user: {}", userId);
        List<CommunityListResponse> communities = communityService.getUserCommunities(userId);
        return ResponseEntity.ok(ApiResponse.success(communities, "User communities fetched successfully"));
    }
}
