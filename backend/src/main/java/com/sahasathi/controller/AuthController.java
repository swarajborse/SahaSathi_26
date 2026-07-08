package com.sahasathi.controller;

import com.sahasathi.dto.ApiResponse;
import com.sahasathi.dto.RegisterRequest;
import com.sahasathi.dto.UserResponse;
import com.sahasathi.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Register/login request received");
        UserResponse response = userService.registerOrLogin(request.getIdToken());
        if (response.isNewUser()) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.created(response, "Registration successful. Please complete your profile."));
        }
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
    }
}
