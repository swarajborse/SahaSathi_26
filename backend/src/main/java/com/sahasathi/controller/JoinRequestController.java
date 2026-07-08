package com.sahasathi.controller;

import com.sahasathi.dto.ApiResponse;
import com.sahasathi.dto.JoinRequestResponse;
import com.sahasathi.model.RequestTargetType;
import com.sahasathi.service.JoinRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/join-requests")
@RequiredArgsConstructor
public class JoinRequestController {

    private final JoinRequestService joinRequestService;

    @PostMapping
    public ResponseEntity<ApiResponse<JoinRequestResponse>> createRequest(
            @RequestParam String targetType,
            @RequestParam Long targetId,
            @RequestParam Long userId) {
        log.info("Create join request: type={}, target={}, user={}", targetType, targetId, userId);
        JoinRequestResponse response = joinRequestService.createRequest(
                RequestTargetType.valueOf(targetType.toUpperCase()), targetId, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response, "Join request sent"));
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<JoinRequestResponse>>> getPendingRequests(
            @RequestParam String targetType,
            @RequestParam Long targetId,
            @RequestParam Long userId) {
        List<JoinRequestResponse> requests = joinRequestService.getPendingRequests(
                RequestTargetType.valueOf(targetType.toUpperCase()), targetId, userId);
        return ResponseEntity.ok(ApiResponse.success(requests, "Pending requests fetched"));
    }

    @PutMapping("/{requestId}/approve")
    public ResponseEntity<ApiResponse<JoinRequestResponse>> approveRequest(
            @PathVariable Long requestId,
            @RequestParam Long userId) {
        JoinRequestResponse response = joinRequestService.approveRequest(requestId, userId);
        return ResponseEntity.ok(ApiResponse.success(response, "Request approved"));
    }

    @PutMapping("/{requestId}/reject")
    public ResponseEntity<ApiResponse<JoinRequestResponse>> rejectRequest(
            @PathVariable Long requestId,
            @RequestParam Long userId) {
        JoinRequestResponse response = joinRequestService.rejectRequest(requestId, userId);
        return ResponseEntity.ok(ApiResponse.success(response, "Request rejected"));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<JoinRequestResponse>>> getMyRequests(
            @RequestParam Long userId) {
        List<JoinRequestResponse> requests = joinRequestService.getMyRequests(userId);
        return ResponseEntity.ok(ApiResponse.success(requests, "Your requests fetched"));
    }
}
