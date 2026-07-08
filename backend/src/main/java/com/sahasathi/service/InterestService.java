package com.sahasathi.service;

import com.sahasathi.dto.InterestResponse;
import com.sahasathi.model.Interest;
import com.sahasathi.repository.InterestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterestService {

    private final InterestRepository interestRepository;

    public List<InterestResponse> getAllInterests() {
        return interestRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public Set<Interest> getInterestsByIds(Set<Long> interestIds) {
        return interestRepository.findAllById(interestIds).stream()
                .collect(Collectors.toSet());
    }

    public List<InterestResponse> getInterestResponses(Set<Interest> interests) {
        return interests.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private InterestResponse mapToResponse(Interest interest) {
        return InterestResponse.builder()
                .id(interest.getId())
                .name(interest.getName())
                .icon(interest.getIcon())
                .build();
    }
}
