package com.sahasathi.service;

import com.sahasathi.exception.BadRequestException;
import com.sahasathi.exception.ResourceNotFoundException;
import com.sahasathi.model.Activity;
import com.sahasathi.model.ActivityFeedback;
import com.sahasathi.model.User;
import com.sahasathi.repository.ActivityFeedbackRepository;
import com.sahasathi.repository.ActivityRepository;
import com.sahasathi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final ActivityFeedbackRepository feedbackRepository;
    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;

    @Transactional
    public ActivityFeedback submitFeedback(Long activityId, Long userId, Integer rating, String comment) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Activity", "id", activityId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (rating < 1 || rating > 5) {
            throw new BadRequestException("Rating must be between 1 and 5");
        }

        if (feedbackRepository.existsByActivityIdAndUserId(activityId, userId)) {
            throw new BadRequestException("You have already submitted feedback for this activity");
        }

        ActivityFeedback feedback = ActivityFeedback.builder()
                .activity(activity)
                .user(user)
                .rating(rating)
                .comment(comment)
                .createdAt(LocalDateTime.now())
                .build();

        ActivityFeedback saved = feedbackRepository.save(feedback);
        log.info("Feedback submitted: {} for activity {} by user {}", saved.getId(), activityId, userId);
        return saved;
    }

    public List<ActivityFeedback> getFeedbackForActivity(Long activityId) {
        return feedbackRepository.findByActivityIdOrderByCreatedAtDesc(activityId);
    }

    public double getAverageRating(Long activityId) {
        List<ActivityFeedback> feedbacks = feedbackRepository.findByActivityIdOrderByCreatedAtDesc(activityId);
        return feedbacks.stream()
                .mapToInt(ActivityFeedback::getRating)
                .average()
                .orElse(0.0);
    }
}
