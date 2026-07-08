package com.sahasathi.controller;

import com.sahasathi.dto.ActivityListResponse;
import com.sahasathi.dto.ActivityResponse;
import com.sahasathi.dto.ApiResponse;
import com.sahasathi.dto.JoinResult;
import com.sahasathi.dto.CreateActivityRequest;
import com.sahasathi.model.ActivityFeedback;
import com.sahasathi.service.ActivityService;
import com.sahasathi.service.FeedbackService;
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

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/activities")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;
    private final FeedbackService feedbackService;

    @PostMapping
    public ResponseEntity<ApiResponse<ActivityResponse>> createActivity(
            @RequestParam Long userId,
            @Valid @RequestBody CreateActivityRequest request) {
        log.info("Creating activity for user: {}", userId);
        ActivityResponse response = activityService.createActivity(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response, "Activity created successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ActivityListResponse>>> listActivities(
            @RequestParam String city,
            @RequestParam(required = false) String locality,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) LocalDateTime fromDate,
            @RequestParam Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Listing activities in city: {}", city);
        Page<ActivityListResponse> activities = activityService.listActivities(
                city, locality, category, fromDate, userId, page, size);
        return ResponseEntity.ok(ApiResponse.success(activities, "Activities fetched successfully"));
    }

    @GetMapping("/{activityId}")
    public ResponseEntity<ApiResponse<ActivityResponse>> getActivity(
            @PathVariable Long activityId,
            @RequestParam Long userId) {
        log.info("Fetching activity: {}", activityId);
        ActivityResponse response = activityService.getActivity(activityId, userId);
        return ResponseEntity.ok(ApiResponse.success(response, "Activity fetched successfully"));
    }

    @PutMapping("/{activityId}")
    public ResponseEntity<ApiResponse<ActivityResponse>> updateActivity(
            @PathVariable Long activityId,
            @RequestParam Long userId,
            @Valid @RequestBody CreateActivityRequest request) {
        log.info("Updating activity: {} by user: {}", activityId, userId);
        ActivityResponse response = activityService.updateActivity(activityId, userId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Activity updated successfully"));
    }

    @DeleteMapping("/{activityId}")
    public ResponseEntity<ApiResponse<Void>> cancelActivity(
            @PathVariable Long activityId,
            @RequestParam Long userId) {
        log.info("Cancelling activity: {} by user: {}", activityId, userId);
        activityService.cancelActivity(activityId, userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Activity cancelled successfully"));
    }

    @PostMapping("/{activityId}/join")
    public ResponseEntity<ApiResponse<JoinResult>> joinActivity(
            @PathVariable Long activityId,
            @RequestParam Long userId) {
        log.info("User {} joining activity: {}", userId, activityId);
        JoinResult result = activityService.joinActivity(activityId, userId);
        String message = "REQUEST_CREATED".equals(result.getType())
                ? "Join request sent for approval" : "Joined activity successfully";
        return ResponseEntity.ok(ApiResponse.success(result, message));
    }

    @PostMapping("/{activityId}/leave")
    public ResponseEntity<ApiResponse<ActivityResponse>> leaveActivity(
            @PathVariable Long activityId,
            @RequestParam Long userId) {
        log.info("User {} leaving activity: {}", userId, activityId);
        ActivityResponse response = activityService.leaveActivity(activityId, userId);
        return ResponseEntity.ok(ApiResponse.success(response, "Left activity successfully"));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<ActivityListResponse>>> getUserActivities(
            @PathVariable Long userId) {
        log.info("Fetching activities for user: {}", userId);
        List<ActivityListResponse> activities = activityService.getUserActivities(userId);
        return ResponseEntity.ok(ApiResponse.success(activities, "User activities fetched successfully"));
    }

    @PostMapping("/{activityId}/feedback")
    public ResponseEntity<ApiResponse<ActivityFeedback>> submitFeedback(
            @PathVariable Long activityId,
            @RequestParam Long userId,
            @RequestParam int rating,
            @RequestParam(required = false) String comment) {
        log.info("User {} submitting feedback for activity {}: rating={}", userId, activityId, rating);
        ActivityFeedback feedback = feedbackService.submitFeedback(activityId, userId, rating, comment);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(feedback, "Feedback submitted successfully"));
    }

    @GetMapping("/{activityId}/feedback")
    public ResponseEntity<ApiResponse<?>> getFeedback(
            @PathVariable Long activityId) {
        log.info("Fetching feedback for activity: {}", activityId);
        var feedback = feedbackService.getFeedbackForActivity(activityId);
        double avgRating = feedbackService.getAverageRating(activityId);
        return ResponseEntity.ok(ApiResponse.success(
                java.util.Map.of("feedback", feedback, "averageRating", Math.round(avgRating * 10.0) / 10.0),
                "Feedback fetched successfully"));
    }

    @GetMapping("/calendar")
    public ResponseEntity<ApiResponse<List<ActivityListResponse>>> getCalendarActivities(
            @RequestParam Long userId,
            @RequestParam int year,
            @RequestParam int month) {
        log.info("Fetching calendar activities for user: {}, year: {}, month: {}", userId, year, month);
        List<ActivityListResponse> activities = activityService.getCalendarActivities(userId, year, month);
        return ResponseEntity.ok(ApiResponse.success(activities, "Calendar activities fetched successfully"));
    }

    @GetMapping("/user/{userId}/managed")
    public ResponseEntity<ApiResponse<Page<ActivityListResponse>>> getManagedActivities(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Fetching managed activities for user: {}", userId);
        Page<ActivityListResponse> activities = activityService.getManagedActivities(userId, page, size);
        return ResponseEntity.ok(ApiResponse.success(activities, "Managed activities fetched successfully"));
    }
}
