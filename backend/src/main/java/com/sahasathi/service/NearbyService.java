package com.sahasathi.service;

import com.sahasathi.dto.InterestResponse;
import com.sahasathi.dto.NearbyUserResponse;
import com.sahasathi.exception.ResourceNotFoundException;
import com.sahasathi.model.Interest;
import com.sahasathi.model.User;
import com.sahasathi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NearbyService {

    private final UserRepository userRepository;
    private final InterestService interestService;

    public Page<NearbyUserResponse> findNearbyUsers(
            Long userId, String localityFilter, Set<Long> interestFilter,
            int page, int size) {

        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Set<Interest> currentUserInterests = currentUser.getInterests();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));

        Page<User> nearbyUsers;
        if (localityFilter != null && !localityFilter.isBlank()) {
            nearbyUsers = userRepository.findNearbyUsers(
                    userId, currentUser.getCity(), localityFilter, pageable);
        } else {
            nearbyUsers = userRepository.findNearbyUsersByCity(
                    userId, currentUser.getCity(), pageable);
        }

        return nearbyUsers.map(user -> mapToNearbyUser(user, currentUserInterests));
    }

    private NearbyUserResponse mapToNearbyUser(User user, Set<Interest> currentUserInterests) {
        Set<Interest> mutual = currentUserInterests.stream()
                .filter(user.getInterests()::contains)
                .collect(Collectors.toSet());

        List<InterestResponse> mutualResponses = mutual.isEmpty()
                ? Collections.emptyList()
                : interestService.getInterestResponses(mutual);

        return NearbyUserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .age(user.getAge())
                .gender(user.getGender() != null ? user.getGender().name() : null)
                .locality(user.getLocality())
                .city(user.getCity())
                .profilePictureUrl(user.getProfilePictureUrl())
                .bio(user.getBio())
                .mutualInterests(mutualResponses)
                .mutualInterestCount(mutual.size())
                .build();
    }
}
