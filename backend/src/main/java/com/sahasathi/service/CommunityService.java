package com.sahasathi.service;

import com.sahasathi.dto.CommunityListResponse;
import com.sahasathi.dto.CommunityResponse;
import com.sahasathi.dto.CreateCommunityRequest;
import com.sahasathi.dto.JoinRequestResponse;
import com.sahasathi.dto.JoinResult;
import com.sahasathi.dto.UserSummary;
import com.sahasathi.exception.AgeVerificationRequiredException;
import com.sahasathi.exception.BadRequestException;
import com.sahasathi.exception.ResourceNotFoundException;
import com.sahasathi.model.Community;
import com.sahasathi.model.CommunityMember;
import com.sahasathi.model.CommunityRole;
import com.sahasathi.model.RequestTargetType;
import com.sahasathi.model.User;
import com.sahasathi.repository.CommunityMemberRepository;
import com.sahasathi.repository.CommunityRepository;
import com.sahasathi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommunityService {

    private final CommunityRepository communityRepository;
    private final CommunityMemberRepository memberRepository;
    private final UserRepository userRepository;
    private final JoinRequestService joinRequestService;

    @Transactional
    public CommunityResponse createCommunity(Long userId, CreateCommunityRequest request) {
        User creator = findUserById(userId);

        Community community = Community.builder()
                .name(request.getName())
                .description(request.getDescription())
                .category(request.getCategory())
                .locality(request.getLocality())
                .city(request.getCity())
                .maxMembers(request.getMaxMembers())
                .isPrivate(request.isPrivate())
                .minAge(request.getMinAge())
                .createdBy(creator)
                .build();

        Community saved = communityRepository.save(community);

        CommunityMember organizer = CommunityMember.builder()
                .community(saved)
                .user(creator)
                .role(CommunityRole.ORGANIZER)
                .joinedAt(LocalDateTime.now())
                .status("ACTIVE")
                .build();
        memberRepository.save(organizer);

        log.info("Community created: {} by user {}", saved.getId(), userId);
        return getCommunityResponse(saved, userId);
    }

    public CommunityResponse getCommunity(Long communityId, Long currentUserId) {
        Community community = findCommunityById(communityId);
        return getCommunityResponse(community, currentUserId);
    }

    public Page<CommunityListResponse> listCommunities(
            String city, String locality, Long currentUserId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Community> communities = communityRepository.findCommunities(city, locality, pageable);
        return communities.map(c -> getListResponse(c, currentUserId));
    }

    @Transactional
    public CommunityResponse updateCommunity(Long communityId, Long userId, CreateCommunityRequest request) {
        Community community = findCommunityById(communityId);
        validateOrganizer(community, userId);

        community.setName(request.getName());
        community.setDescription(request.getDescription());
        community.setCategory(request.getCategory());
        community.setLocality(request.getLocality());
        community.setCity(request.getCity());
        community.setMaxMembers(request.getMaxMembers());
        community.setPrivate(request.isPrivate());
        community.setMinAge(request.getMinAge());

        Community saved = communityRepository.save(community);
        return getCommunityResponse(saved, userId);
    }

    @Transactional
    public void deleteCommunity(Long communityId, Long userId) {
        Community community = findCommunityById(communityId);
        validateOrganizer(community, userId);
        communityRepository.delete(community);
        log.info("Community deleted: {}", communityId);
    }

    @Transactional
    public JoinResult joinCommunity(Long communityId, Long userId) {
        Community community = findCommunityById(communityId);
        User user = findUserById(userId);

        if (memberRepository.existsByCommunityIdAndUserIdAndStatus(communityId, userId, "ACTIVE")) {
            throw new BadRequestException("You are already a member of this community");
        }

        if (community.getMaxMembers() != null) {
            long count = memberRepository.countByCommunityIdAndStatus(communityId, "ACTIVE");
            if (count >= community.getMaxMembers()) {
                throw new BadRequestException("Community is full");
            }
        }

        if (community.isPrivate() && community.getMinAge() != null && community.getMinAge() >= 55) {
            if (!user.isAgeVerified()) {
                throw new AgeVerificationRequiredException();
            }
            if (user.getAge() == null || user.getAge() < community.getMinAge()) {
                throw new BadRequestException("You must be at least " + community.getMinAge() + " years old");
            }
        }

        if (community.isPrivate()) {
            JoinRequestResponse requestResponse = joinRequestService.createRequest(
                    RequestTargetType.COMMUNITY, communityId, userId);
            return JoinResult.builder().type("REQUEST_CREATED").data(requestResponse).build();
        }

        CommunityMember member = CommunityMember.builder()
                .community(community)
                .user(user)
                .role(CommunityRole.MEMBER)
                .joinedAt(LocalDateTime.now())
                .status("ACTIVE")
                .build();
        memberRepository.save(member);

        log.info("User {} joined community {}", userId, communityId);
        return JoinResult.builder().type("JOINED").data(getCommunityResponse(community, userId)).build();
    }

    @Transactional
    public CommunityResponse leaveCommunity(Long communityId, Long userId) {
        Community community = findCommunityById(communityId);

        if (memberRepository.existsByCommunityIdAndUserIdAndRole(communityId, userId, CommunityRole.ORGANIZER)) {
            throw new BadRequestException("Organizer cannot leave. Transfer ownership or delete the community.");
        }

        CommunityMember member = memberRepository
                .findByCommunityIdAndUserId(communityId, userId)
                .orElseThrow(() -> new BadRequestException("You are not a member of this community"));

        member.setStatus("LEFT");
        memberRepository.save(member);

        log.info("User {} left community {}", userId, communityId);
        return getCommunityResponse(community, userId);
    }

    public List<CommunityListResponse> getUserCommunities(Long userId) {
        List<CommunityMember> memberships = memberRepository
                .findByUserIdAndStatusOrderByJoinedAtDesc(userId, "ACTIVE");
        return memberships.stream()
                .map(m -> getListResponse(m.getCommunity(), userId))
                .collect(Collectors.toList());
    }

    private CommunityResponse getCommunityResponse(Community community, Long currentUserId) {
        List<CommunityMember> activeMembers = memberRepository
                .findByCommunityIdAndStatus(community.getId(), "ACTIVE");

        List<UserSummary> memberSummaries = activeMembers.stream()
                .map(m -> UserSummary.builder()
                        .id(m.getUser().getId())
                        .name(m.getUser().getName())
                        .age(m.getUser().getAge())
                        .gender(m.getUser().getGender() != null ? m.getUser().getGender().name() : null)
                        .locality(m.getUser().getLocality())
                        .profilePictureUrl(m.getUser().getProfilePictureUrl())
                        .build())
                .collect(Collectors.toList());

        boolean isJoined = activeMembers.stream().anyMatch(m -> m.getUser().getId().equals(currentUserId));
        boolean isOrganizer = memberRepository
                .existsByCommunityIdAndUserIdAndRole(community.getId(), currentUserId, CommunityRole.ORGANIZER);

        return CommunityResponse.builder()
                .id(community.getId())
                .name(community.getName())
                .description(community.getDescription())
                .category(community.getCategory())
                .locality(community.getLocality())
                .city(community.getCity())
                .coverImageUrl(community.getCoverImageUrl())
                .maxMembers(community.getMaxMembers())
                .memberCount(activeMembers.size())
                .isPrivate(community.isPrivate())
                .minAge(community.getMinAge())
                .isJoined(isJoined)
                .isOrganizer(isOrganizer)
                .creator(UserSummary.builder()
                        .id(community.getCreatedBy().getId())
                        .name(community.getCreatedBy().getName())
                        .profilePictureUrl(community.getCreatedBy().getProfilePictureUrl())
                        .build())
                .members(memberSummaries)
                .createdAt(community.getCreatedAt())
                .build();
    }

    private CommunityListResponse getListResponse(Community community, Long currentUserId) {
        long memberCount = memberRepository.countByCommunityIdAndStatus(community.getId(), "ACTIVE");
        boolean isJoined = memberRepository
                .existsByCommunityIdAndUserIdAndStatus(community.getId(), currentUserId, "ACTIVE");

        return CommunityListResponse.builder()
                .id(community.getId())
                .name(community.getName())
                .category(community.getCategory())
                .locality(community.getLocality())
                .city(community.getCity())
                .memberCount((int) memberCount)
                .maxMembers(community.getMaxMembers())
                .isPrivate(community.isPrivate())
                .creatorName(community.getCreatedBy().getName())
                .isJoined(isJoined)
                .createdAt(community.getCreatedAt())
                .build();
    }

    private void validateOrganizer(Community community, Long userId) {
        if (!memberRepository.existsByCommunityIdAndUserIdAndRole(community.getId(), userId, CommunityRole.ORGANIZER)) {
            throw new BadRequestException("Only organizers can perform this action");
        }
    }

    private Community findCommunityById(Long id) {
        return communityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Community", "id", id));
    }

    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }
}
