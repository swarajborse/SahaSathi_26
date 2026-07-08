package com.sahasathi.service;

import com.sahasathi.dto.ActivityListResponse;
import com.sahasathi.dto.ActivityResponse;
import com.sahasathi.dto.CreateActivityRequest;
import com.sahasathi.dto.JoinRequestResponse;
import com.sahasathi.dto.JoinResult;
import com.sahasathi.dto.UserSummary;
import com.sahasathi.exception.AgeVerificationRequiredException;
import com.sahasathi.exception.BadRequestException;
import com.sahasathi.exception.ResourceNotFoundException;
import com.sahasathi.model.Activity;
import com.sahasathi.model.ActivityParticipant;
import com.sahasathi.model.ActivityStatus;
import com.sahasathi.model.ParticipantStatus;
import com.sahasathi.model.RequestTargetType;
import com.sahasathi.model.User;
import com.sahasathi.repository.ActivityParticipantRepository;
import com.sahasathi.repository.ActivityRepository;
import com.sahasathi.repository.UserRepository;
import com.sahasathi.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final ActivityParticipantRepository participantRepository;
    private final UserRepository userRepository;
    private final JoinRequestService joinRequestService;
    private final NotificationService notificationService;

    @Transactional
    public ActivityResponse createActivity(Long userId, CreateActivityRequest request) {
        User creator = findUserById(userId);

        Activity activity = Activity.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .dateTime(request.getDateTime())
                .duration(request.getDuration())
                .location(request.getLocation())
                .locality(request.getLocality())
                .city(request.getCity())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .maxParticipants(request.getMaxParticipants())
                .createdBy(creator)
                .isPrivate(request.isPrivate())
                .minAge(request.getMinAge())
                .status(ActivityStatus.UPCOMING)
                .build();

        Activity saved = activityRepository.save(activity);

        joinActivity(saved.getId(), userId);

        log.info("Activity created: {} by user {}", saved.getId(), userId);
        return getActivityResponse(saved, userId);
    }

    public ActivityResponse getActivity(Long activityId, Long currentUserId) {
        Activity activity = findActivityById(activityId);
        return getActivityResponse(activity, currentUserId);
    }

    public Page<ActivityListResponse> listActivities(
            String city, String locality, String category,
            LocalDateTime fromDate, Long currentUserId, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        if (fromDate == null) {
            fromDate = LocalDateTime.now();
        }

        Page<Activity> activities = activityRepository.findActivities(
                city, locality, category, fromDate, pageable);

        return activities.map(a -> getListResponse(a, currentUserId));
    }

    @Transactional
    public ActivityResponse updateActivity(Long activityId, Long userId, CreateActivityRequest request) {
        Activity activity = findActivityById(activityId);
        validateOwnership(activity, userId);

        activity.setTitle(request.getTitle());
        activity.setDescription(request.getDescription());
        activity.setCategory(request.getCategory());
        activity.setDateTime(request.getDateTime());
        activity.setDuration(request.getDuration());
        activity.setLocation(request.getLocation());
        activity.setLocality(request.getLocality());
        activity.setCity(request.getCity());
        activity.setLatitude(request.getLatitude());
        activity.setLongitude(request.getLongitude());
        activity.setMaxParticipants(request.getMaxParticipants());
        activity.setPrivate(request.isPrivate());
        activity.setMinAge(request.getMinAge());

        Activity saved = activityRepository.save(activity);
        log.info("Activity updated: {}", activityId);
        return getActivityResponse(saved, userId);
    }

    public List<ActivityListResponse> getCalendarActivities(Long userId, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime end = yearMonth.atEndOfMonth().plusDays(1).atStartOfDay();

        return activityRepository.findActivitiesBetween(start, end).stream()
                .map(a -> getListResponse(a, userId))
                .collect(Collectors.toList());
    }

    @Transactional
    public void cancelActivity(Long activityId, Long userId) {
        Activity activity = findActivityById(activityId);
        validateOwnership(activity, userId);
        activity.setStatus(ActivityStatus.CANCELLED);
        activityRepository.save(activity);

        List<ActivityParticipant> participants = participantRepository
                .findByActivityIdAndStatus(activityId, ParticipantStatus.JOINED);
        for (ActivityParticipant p : participants) {
            if (!p.getUser().getId().equals(userId)) {
                notificationService.createNotification(
                        p.getUser().getId(), "ACTIVITY_CANCELLED",
                        "Activity Cancelled",
                        "The activity \"" + activity.getTitle() + "\" has been cancelled.",
                        "ACTIVITY", activityId);
            }
        }

        log.info("Activity cancelled: {}, notified {} participants", activityId, participants.size());
    }

    @Transactional
    public JoinResult joinActivity(Long activityId, Long userId) {
        Activity activity = findActivityById(activityId);
        User user = findUserById(userId);

        if (activity.getStatus() != ActivityStatus.UPCOMING) {
            throw new BadRequestException("Cannot join a " + activity.getStatus().name().toLowerCase() + " activity");
        }

        if (participantRepository.existsByActivityIdAndUserIdAndStatus(activityId, userId, ParticipantStatus.JOINED)) {
            throw new BadRequestException("You have already joined this activity");
        }

        if (activity.getMaxParticipants() != null) {
            long currentCount = participantRepository.countByActivityIdAndStatus(activityId, ParticipantStatus.JOINED);
            if (currentCount >= activity.getMaxParticipants()) {
                throw new BadRequestException("Activity is full");
            }
        }

        if (activity.isPrivate() && activity.getMinAge() != null && activity.getMinAge() >= 55) {
            if (!user.isAgeVerified()) {
                throw new AgeVerificationRequiredException();
            }
            if (user.getAge() == null || user.getAge() < activity.getMinAge()) {
                throw new BadRequestException("You must be at least " + activity.getMinAge() + " years old to join");
            }
        }

        if (activity.isPrivate()) {
            JoinRequestResponse requestResponse = joinRequestService.createRequest(
                    RequestTargetType.ACTIVITY, activityId, userId);
            return JoinResult.builder().type("REQUEST_CREATED").data(requestResponse).build();
        }

        ActivityParticipant participant = ActivityParticipant.builder()
                .activity(activity)
                .user(user)
                .joinedAt(LocalDateTime.now())
                .status(ParticipantStatus.JOINED)
                .build();

        participantRepository.save(participant);
        log.info("User {} joined activity {}", userId, activityId);
        return JoinResult.builder().type("JOINED").data(getActivityResponse(activity, userId)).build();
    }

    @Transactional
    public ActivityResponse leaveActivity(Long activityId, Long userId) {
        Activity activity = findActivityById(activityId);

        if (activity.getCreatedBy().getId().equals(userId)) {
            throw new BadRequestException("Activity creator cannot leave. Cancel the activity instead.");
        }

        ActivityParticipant participant = participantRepository
                .findByActivityIdAndUserId(activityId, userId)
                .orElseThrow(() -> new BadRequestException("You have not joined this activity"));

        participant.setStatus(ParticipantStatus.CANCELLED);
        participantRepository.save(participant);
        log.info("User {} left activity {}", userId, activityId);
        return getActivityResponse(activity, userId);
    }

    public List<ActivityListResponse> getUserActivities(Long userId) {
        List<ActivityParticipant> participations = participantRepository
                .findByUserIdAndStatusOrderByJoinedAtDesc(userId, ParticipantStatus.JOINED);

        return participations.stream()
                .map(p -> getListResponse(p.getActivity(), userId))
                .collect(Collectors.toList());
    }

    public Page<ActivityListResponse> getManagedActivities(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return activityRepository.findByCreatedByIdOrderByCreatedAtDesc(userId, pageable)
                .map(a -> getListResponse(a, userId));
    }

    private ActivityResponse getActivityResponse(Activity activity, Long currentUserId) {
        List<ActivityParticipant> activeParticipants = participantRepository
                .findByActivityIdAndStatus(activity.getId(), ParticipantStatus.JOINED);

        List<UserSummary> participantSummaries = activeParticipants.stream()
                .map(ap -> UserSummary.builder()
                        .id(ap.getUser().getId())
                        .name(ap.getUser().getName())
                        .age(ap.getUser().getAge())
                        .gender(ap.getUser().getGender() != null ? ap.getUser().getGender().name() : null)
                        .locality(ap.getUser().getLocality())
                        .profilePictureUrl(ap.getUser().getProfilePictureUrl())
                        .build())
                .collect(Collectors.toList());

        boolean isJoined = activeParticipants.stream()
                .anyMatch(ap -> ap.getUser().getId().equals(currentUserId));

        return ActivityResponse.builder()
                .id(activity.getId())
                .title(activity.getTitle())
                .description(activity.getDescription())
                .category(activity.getCategory())
                .dateTime(activity.getDateTime())
                .duration(activity.getDuration())
                .location(activity.getLocation())
                .locality(activity.getLocality())
                .city(activity.getCity())
                .latitude(activity.getLatitude())
                .longitude(activity.getLongitude())
                .maxParticipants(activity.getMaxParticipants())
                .participantCount(activeParticipants.size())
                .status(activity.getStatus().name())
                .isPrivate(activity.isPrivate())
                .minAge(activity.getMinAge())
                .imageUrl(activity.getImageUrl())
                .isJoined(isJoined)
                .isCreator(activity.getCreatedBy().getId().equals(currentUserId))
                .creator(UserSummary.builder()
                        .id(activity.getCreatedBy().getId())
                        .name(activity.getCreatedBy().getName())
                        .profilePictureUrl(activity.getCreatedBy().getProfilePictureUrl())
                        .build())
                .participants(participantSummaries)
                .createdAt(activity.getCreatedAt())
                .build();
    }

    private ActivityListResponse getListResponse(Activity activity, Long currentUserId) {
        long participantCount = participantRepository
                .countByActivityIdAndStatus(activity.getId(), ParticipantStatus.JOINED);

        boolean isJoined = participantRepository
                .existsByActivityIdAndUserIdAndStatus(activity.getId(), currentUserId, ParticipantStatus.JOINED);

        return ActivityListResponse.builder()
                .id(activity.getId())
                .title(activity.getTitle())
                .category(activity.getCategory())
                .dateTime(activity.getDateTime())
                .duration(activity.getDuration())
                .location(activity.getLocation())
                .locality(activity.getLocality())
                .city(activity.getCity())
                .maxParticipants(activity.getMaxParticipants())
                .participantCount((int) participantCount)
                .status(activity.getStatus().name())
                .isPrivate(activity.isPrivate())
                .creatorName(activity.getCreatedBy().getName())
                .creatorProfilePicture(activity.getCreatedBy().getProfilePictureUrl())
                .isJoined(isJoined)
                .build();
    }

    private void validateOwnership(Activity activity, Long userId) {
        if (!activity.getCreatedBy().getId().equals(userId)) {
            throw new BadRequestException("Only the activity creator can perform this action");
        }
    }

    private Activity findActivityById(Long id) {
        return activityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Activity", "id", id));
    }

    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }
}
