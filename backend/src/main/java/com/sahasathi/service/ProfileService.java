package com.sahasathi.service;

import com.sahasathi.dto.InterestResponse;
import com.sahasathi.dto.InterestUpdateRequest;
import com.sahasathi.dto.ProfileCompletenessResponse;
import com.sahasathi.dto.UserResponse;
import com.sahasathi.exception.ResourceNotFoundException;
import com.sahasathi.model.Interest;
import com.sahasathi.model.User;
import com.sahasathi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final InterestService interestService;
    private final FileStorageService fileStorageService;

    public List<InterestResponse> getUserInterests(Long userId) {
        User user = findUserById(userId);
        return interestService.getInterestResponses(user.getInterests());
    }

    @Transactional
    public List<InterestResponse> updateUserInterests(Long userId, InterestUpdateRequest request) {
        User user = findUserById(userId);
        Set<Interest> interests = interestService.getInterestsByIds(request.getInterestIds());
        user.setInterests(interests);
        userRepository.save(user);
        log.info("Interests updated for user: {}", userId);
        return interestService.getInterestResponses(interests);
    }

    @Transactional
    public UserResponse uploadProfilePicture(Long userId, MultipartFile file) {
        User user = findUserById(userId);
        String filename = fileStorageService.storeFile(file);
        user.setProfilePictureUrl(filename);
        userRepository.save(user);
        log.info("Profile picture updated for user: {}", userId);
        return mapToUserResponse(user);
    }

    public ProfileCompletenessResponse getProfileCompleteness(Long userId) {
        User user = findUserById(userId);

        boolean nameFilled = user.getName() != null && !user.getName().isEmpty() && !user.getName().equals("User");
        boolean dobFilled = user.getDateOfBirth() != null;
        boolean genderFilled = user.getGender() != null;
        boolean localityFilled = user.getLocality() != null && !user.getLocality().isEmpty();
        boolean cityFilled = user.getCity() != null && !user.getCity().isEmpty();
        boolean bioFilled = user.getBio() != null && !user.getBio().isEmpty();
        boolean hasInterests = user.getInterests() != null && !user.getInterests().isEmpty();
        boolean hasProfilePicture = user.getProfilePictureUrl() != null && !user.getProfilePictureUrl().isEmpty();

        int filled = 0;
        int total = 8;
        for (boolean b : new boolean[]{nameFilled, dobFilled, genderFilled, localityFilled,
                cityFilled, bioFilled, hasInterests, hasProfilePicture}) {
            if (b) filled++;
        }

        return ProfileCompletenessResponse.builder()
                .percentage((filled * 100) / total)
                .nameFilled(nameFilled)
                .dobFilled(dobFilled)
                .genderFilled(genderFilled)
                .localityFilled(localityFilled)
                .cityFilled(cityFilled)
                .bioFilled(bioFilled)
                .hasInterests(hasInterests)
                .hasProfilePicture(hasProfilePicture)
                .build();
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .phoneNumber(user.getPhoneNumber())
                .name(user.getName())
                .email(user.getEmail())
                .dateOfBirth(user.getDateOfBirth())
                .age(user.getAge())
                .gender(user.getGender() != null ? user.getGender().name() : null)
                .bio(user.getBio())
                .locality(user.getLocality())
                .city(user.getCity())
                .state(user.getState())
                .latitude(user.getLatitude())
                .longitude(user.getLongitude())
                .profilePictureUrl(user.getProfilePictureUrl())
                .ageVerified(user.isAgeVerified())
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
