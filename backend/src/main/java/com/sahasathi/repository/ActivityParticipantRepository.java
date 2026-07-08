package com.sahasathi.repository;

import com.sahasathi.model.ActivityParticipant;
import com.sahasathi.model.ParticipantStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ActivityParticipantRepository extends JpaRepository<ActivityParticipant, Long> {

    List<ActivityParticipant> findByActivityIdAndStatus(Long activityId, ParticipantStatus status);

    Optional<ActivityParticipant> findByActivityIdAndUserId(Long activityId, Long userId);

    long countByActivityIdAndStatus(Long activityId, ParticipantStatus status);

    boolean existsByActivityIdAndUserIdAndStatus(Long activityId, Long userId, ParticipantStatus status);

    List<ActivityParticipant> findByUserIdAndStatusOrderByJoinedAtDesc(Long userId, ParticipantStatus status);
}
