package com.sahasathi.service;

import com.sahasathi.dto.SearchResult;
import com.sahasathi.model.Activity;
import com.sahasathi.model.ActivityParticipant;
import com.sahasathi.model.Community;
import com.sahasathi.model.CommunityMember;
import com.sahasathi.model.User;
import com.sahasathi.repository.ActivityParticipantRepository;
import com.sahasathi.repository.ActivityRepository;
import com.sahasathi.repository.CommunityMemberRepository;
import com.sahasathi.repository.CommunityRepository;
import com.sahasathi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private final ActivityRepository activityRepository;
    private final CommunityRepository communityRepository;
    private final UserRepository userRepository;
    private final ActivityParticipantRepository participantRepository;
    private final CommunityMemberRepository memberRepository;

    public Page<SearchResult> search(String keyword, String type, String city, String locality,
                                     int page, int size) {
        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        Pageable pageable = PageRequest.of(page, size);

        List<SearchResult> results = new ArrayList<>();

        if (type == null || type.equals("all") || type.equals("activities")) {
            Page<Activity> activities = activityRepository.searchActivities(kw, city, locality, null, pageable);
            for (Activity a : activities.getContent()) {
                int participantCount = (int) participantRepository.countByActivityIdAndStatus(a.getId(), com.sahasathi.model.ParticipantStatus.JOINED);
                results.add(SearchResult.builder()
                        .type("ACTIVITY")
                        .id(a.getId())
                        .title(a.getTitle())
                        .subtitle(a.getDescription())
                        .imageUrl(a.getImageUrl())
                        .city(a.getCity())
                        .locality(a.getLocality())
                        .category(a.getCategory())
                        .dateTime(a.getDateTime())
                        .participantCount(participantCount)
                        .isPrivate(a.isPrivate())
                        .build());
            }
        }

        if (type == null || type.equals("all") || type.equals("communities")) {
            Page<Community> communities = communityRepository.searchCommunities(kw, city, locality, pageable);
            for (Community c : communities.getContent()) {
                int memberCount = (int) memberRepository.countByCommunityIdAndStatus(c.getId(), "ACTIVE");
                results.add(SearchResult.builder()
                        .type("COMMUNITY")
                        .id(c.getId())
                        .title(c.getName())
                        .subtitle(c.getDescription())
                        .imageUrl(c.getCoverImageUrl())
                        .city(c.getCity())
                        .locality(c.getLocality())
                        .category(c.getCategory())
                        .memberCount(memberCount)
                        .isPrivate(c.isPrivate())
                        .build());
            }
        }

        if (type == null || type.equals("all") || type.equals("users")) {
            Page<User> users = userRepository.searchUsers(kw, city, locality, pageable);
            for (User u : users.getContent()) {
                results.add(SearchResult.builder()
                        .type("USER")
                        .id(u.getId())
                        .title(u.getName())
                        .subtitle(u.getBio())
                        .imageUrl(u.getProfilePictureUrl())
                        .city(u.getCity())
                        .locality(u.getLocality())
                        .build());
            }
        }

        results.sort(Comparator.comparing(SearchResult::getType));
        int start = page * size;
        int end = Math.min(start + size, results.size());
        List<SearchResult> pageResults = start < results.size() ? results.subList(start, end) : List.of();

        return new PageImpl<>(pageResults, pageable, results.size());
    }
}
