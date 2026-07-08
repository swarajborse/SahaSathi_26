package com.sahasathi.controller;

import com.sahasathi.dto.ApiResponse;
import com.sahasathi.dto.NearbyUserResponse;
import com.sahasathi.service.NearbyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class NearbyController {

    private final NearbyService nearbyService;

    @GetMapping("/{userId}/nearby")
    public ResponseEntity<ApiResponse<Page<NearbyUserResponse>>> getNearbyUsers(
            @PathVariable Long userId,
            @RequestParam(required = false) String locality,
            @RequestParam(required = false) String interests,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("Fetching nearby users for userId: {}, locality: {}", userId, locality);

        Set<Long> interestFilter = null;
        if (interests != null && !interests.isBlank()) {
            interestFilter = Set.of(interests.split(",")).stream()
                    .map(String::trim)
                    .map(Long::parseLong)
                    .collect(Collectors.toSet());
        }

        Page<NearbyUserResponse> nearbyUsers = nearbyService.findNearbyUsers(
                userId, locality, interestFilter, page, size);

        return ResponseEntity.ok(ApiResponse.success(nearbyUsers, "Nearby users fetched successfully"));
    }
}
