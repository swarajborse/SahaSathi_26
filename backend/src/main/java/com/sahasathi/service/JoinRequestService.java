package com.sahasathi.service;

import com.sahasathi.dto.JoinRequestResponse;
import com.sahasathi.dto.UserSummary;
import com.sahasathi.exception.BadRequestException;
import com.sahasathi.exception.ResourceNotFoundException;
import com.sahasathi.model.Activity;
import com.sahasathi.model.Community;
import com.sahasathi.model.CommunityMember;
import com.sahasathi.model.CommunityRole;
import com.sahasathi.model.JoinRequest;
import com.sahasathi.model.ParticipantStatus;
import com.sahasathi.model.RequestStatus;
import com.sahasathi.model.RequestTargetType;
import com.sahasathi.model.User;
import com.sahasathi.repository.ActivityParticipantRepository;
import com.sahasathi.repository.ActivityRepository;
import com.sahasathi.repository.CommunityMemberRepository;
import com.sahasathi.repository.CommunityRepository;
import com.sahasathi.repository.JoinRequestRepository;
import com.sahasathi.repository.UserRepository;
import com.sahasathi.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class JoinRequestService {

    private final JoinRequestRepository joinRequestRepository;
    private final CommunityRepository communityRepository;
    private final CommunityMemberRepository memberRepository;
    private final ActivityRepository activityRepository;
    private final ActivityParticipantRepository participantRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public JoinRequestResponse createRequest(RequestTargetType targetType, Long targetId, Long requesterId) {
        if (joinRequestRepository.existsByTargetTypeAndTargetIdAndRequesterIdAndStatus(
                targetType, targetId, requesterId, RequestStatus.PENDING)) {
            throw new BadRequestException("You already have a pending request for this");
        }

        String targetName = getTargetName(targetType, targetId);

        JoinRequest request = JoinRequest.builder()
                .targetType(targetType)
                .targetId(targetId)
                .requesterId(requesterId)
                .status(RequestStatus.PENDING)
                .build();

        JoinRequest saved = joinRequestRepository.save(request);
        log.info("Join request created: type={}, targetId={}, requester={}", targetType, targetId, requesterId);
        return mapToResponse(saved, targetName);
    }

    @Transactional
    public JoinRequestResponse approveRequest(Long requestId, Long reviewerId) {
        JoinRequest request = findRequestById(requestId);
        validateReviewer(request, reviewerId);

        request.setStatus(RequestStatus.APPROVED);
        request.setReviewedById(reviewerId);
        request.setReviewedAt(LocalDateTime.now());
        joinRequestRepository.save(request);

        if (request.getTargetType() == RequestTargetType.COMMUNITY) {
            Community community = communityRepository.findById(request.getTargetId())
                    .orElseThrow(() -> new ResourceNotFoundException("Community", "id", request.getTargetId()));
            User user = findUserById(request.getRequesterId());

            CommunityMember member = CommunityMember.builder()
                    .community(community)
                    .user(user)
                    .role(CommunityRole.MEMBER)
                    .joinedAt(LocalDateTime.now())
                    .status("ACTIVE")
                    .build();
            memberRepository.save(member);
        } else {
            Activity activity = activityRepository.findById(request.getTargetId())
                    .orElseThrow(() -> new ResourceNotFoundException("Activity", "id", request.getTargetId()));
            User user = findUserById(request.getRequesterId());

            com.sahasathi.model.ActivityParticipant participant =
                    com.sahasathi.model.ActivityParticipant.builder()
                            .activity(activity)
                            .user(user)
                            .joinedAt(LocalDateTime.now())
                            .status(ParticipantStatus.JOINED)
                            .build();
            participantRepository.save(participant);
        }

        String targetName = getTargetName(request.getTargetType(), request.getTargetId());
        notificationService.createNotification(
                request.getRequesterId(), "JOIN_APPROVED",
                "Request Approved",
                "Your request to join " + targetName + " has been approved.",
                request.getTargetType().name(), request.getTargetId());

        log.info("Join request approved: {}", requestId);
        return mapToResponse(request, targetName);
    }

    @Transactional
    public JoinRequestResponse rejectRequest(Long requestId, Long reviewerId) {
        JoinRequest request = findRequestById(requestId);
        validateReviewer(request, reviewerId);

        request.setStatus(RequestStatus.REJECTED);
        request.setReviewedById(reviewerId);
        request.setReviewedAt(LocalDateTime.now());
        joinRequestRepository.save(request);

        String targetName = getTargetName(request.getTargetType(), request.getTargetId());
        notificationService.createNotification(
                request.getRequesterId(), "JOIN_REJECTED",
                "Request Rejected",
                "Your request to join " + targetName + " has been rejected.",
                request.getTargetType().name(), request.getTargetId());

        log.info("Join request rejected: {}", requestId);
        return mapToResponse(request, targetName);
    }

    public List<JoinRequestResponse> getPendingRequests(RequestTargetType targetType, Long targetId, Long reviewerId) {
        validateReviewer(targetType, targetId, reviewerId);

        List<JoinRequest> requests = joinRequestRepository
                .findByTargetTypeAndTargetIdAndStatusOrderByCreatedAtDesc(
                        targetType, targetId, RequestStatus.PENDING);

        String targetName = getTargetName(targetType, targetId);
        return requests.stream().map(r -> mapToResponse(r, targetName)).collect(Collectors.toList());
    }

    public List<JoinRequestResponse> getMyRequests(Long userId) {
        List<JoinRequest> requests = joinRequestRepository
                .findByRequesterIdOrderByCreatedAtDesc(userId);

        return requests.stream().map(r -> mapToResponse(r,
                getTargetName(r.getTargetType(), r.getTargetId()))).collect(Collectors.toList());
    }

    private void validateReviewer(JoinRequest request, Long reviewerId) {
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new BadRequestException("This request has already been " + request.getStatus().name().toLowerCase());
        }
        validateReviewer(request.getTargetType(), request.getTargetId(), reviewerId);
    }

    private void validateReviewer(RequestTargetType targetType, Long targetId, Long reviewerId) {
        if (targetType == RequestTargetType.COMMUNITY) {
            if (!memberRepository.existsByCommunityIdAndUserIdAndRole(targetId, reviewerId, CommunityRole.ORGANIZER)) {
                throw new BadRequestException("Only community organizers can manage join requests");
            }
        } else {
            Activity activity = activityRepository.findById(targetId)
                    .orElseThrow(() -> new ResourceNotFoundException("Activity", "id", targetId));
            if (!activity.getCreatedBy().getId().equals(reviewerId)) {
                throw new BadRequestException("Only the activity creator can manage join requests");
            }
        }
    }

    private String getTargetName(RequestTargetType type, Long targetId) {
        if (type == RequestTargetType.COMMUNITY) {
            return communityRepository.findById(targetId)
                    .map(Community::getName)
                    .orElse("Unknown");
        }
        return activityRepository.findById(targetId)
                .map(Activity::getTitle)
                .orElse("Unknown");
    }

    private JoinRequestResponse mapToResponse(JoinRequest request, String targetName) {
        User requester = findUserById(request.getRequesterId());
        return JoinRequestResponse.builder()
                .id(request.getId())
                .targetType(request.getTargetType().name())
                .targetId(request.getTargetId())
                .targetName(targetName)
                .requester(UserSummary.builder()
                        .id(requester.getId())
                        .name(requester.getName())
                        .age(requester.getAge())
                        .profilePictureUrl(requester.getProfilePictureUrl())
                        .build())
                .status(request.getStatus().name())
                .createdAt(request.getCreatedAt())
                .build();
    }

    private JoinRequest findRequestById(Long id) {
        return joinRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("JoinRequest", "id", id));
    }

    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }
}
