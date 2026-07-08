package com.sahasathi.controller;

import com.sahasathi.dto.ApiResponse;
import com.sahasathi.service.AgeVerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class AgeVerificationController {

    private final AgeVerificationService ageVerificationService;

    @PostMapping("/{userId}/verify-age")
    public ResponseEntity<ApiResponse<Map<String, Object>>> verifyAge(
            @PathVariable Long userId,
            @RequestParam("aadhaarImage") MultipartFile aadhaarImage,
            @RequestParam(required = false) String dob) {

        log.info("Age verification request for user: {}", userId);

        LocalDate confirmedDob = dob != null ? LocalDate.parse(dob) : null;
        AgeVerificationService.VerificationResult result =
                ageVerificationService.verifyAge(userId, aadhaarImage, confirmedDob);

        Map<String, Object> data = Map.of(
                "verified", result.success(),
                "age", result.age(),
                "message", result.message()
        );

        return ResponseEntity.ok(ApiResponse.success(data, result.message()));
    }

    @GetMapping("/{userId}/age-verification-status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getVerificationStatus(
            @PathVariable Long userId) {
        boolean verified = ageVerificationService.isAgeVerified(userId);
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("verified", verified),
                verified ? "Age verified" : "Age not verified"));
    }
}
