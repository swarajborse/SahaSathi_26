package com.sahasathi.repository;

import com.sahasathi.model.ActivityFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ActivityFeedbackRepository extends JpaRepository<ActivityFeedback, Long> {

    List<ActivityFeedback> findByActivityIdOrderByCreatedAtDesc(Long activityId);

    Optional<ActivityFeedback> findByActivityIdAndUserId(Long activityId, Long userId);

    boolean existsByActivityIdAndUserId(Long activityId, Long userId);
}
