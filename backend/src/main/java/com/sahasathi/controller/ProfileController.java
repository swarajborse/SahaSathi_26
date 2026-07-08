package com.sahasathi.controller;

import com.sahasathi.dto.ApiResponse;
import com.sahasathi.dto.InterestResponse;
import com.sahasathi.dto.InterestUpdateRequest;
import com.sahasathi.dto.ProfileCompletenessResponse;
import com.sahasathi.dto.UserResponse;
import com.sahasathi.service.InterestService;
import com.sahasathi.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ProfileController {

    private final InterestService interestService;
    private final ProfileService profileService;

    @GetMapping("/interests")
    public ResponseEntity<ApiResponse<List<InterestResponse>>> getAllInterests() {
        List<InterestResponse> interests = interestService.getAllInterests();
        return ResponseEntity.ok(ApiResponse.success(interests, "Interests fetched successfully"));
    }

    @GetMapping("/users/{userId}/interests")
    public ResponseEntity<ApiResponse<List<InterestResponse>>> getUserInterests(@PathVariable Long userId) {
        List<InterestResponse> interests = profileService.getUserInterests(userId);
        return ResponseEntity.ok(ApiResponse.success(interests, "User interests fetched successfully"));
    }

    @PutMapping("/users/{userId}/interests")
    public ResponseEntity<ApiResponse<List<InterestResponse>>> updateUserInterests(
            @PathVariable Long userId,
            @Valid @RequestBody InterestUpdateRequest request) {
        List<InterestResponse> interests = profileService.updateUserInterests(userId, request);
        return ResponseEntity.ok(ApiResponse.success(interests, "Interests updated successfully"));
    }

    @PostMapping(value = "/users/{userId}/profile-picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UserResponse>> uploadProfilePicture(
            @PathVariable Long userId,
            @RequestParam("file") MultipartFile file) {
        UserResponse response = profileService.uploadProfilePicture(userId, file);
        return ResponseEntity.ok(ApiResponse.success(response, "Profile picture uploaded successfully"));
    }

    @GetMapping("/users/{userId}/profile-completeness")
    public ResponseEntity<ApiResponse<ProfileCompletenessResponse>> getProfileCompleteness(
            @PathVariable Long userId) {
        ProfileCompletenessResponse completeness = profileService.getProfileCompleteness(userId);
        return ResponseEntity.ok(ApiResponse.success(completeness, "Profile completeness fetched"));
    }
}
