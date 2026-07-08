package com.sahasathi.service;

import com.sahasathi.dto.UpdateProfileRequest;
import com.sahasathi.dto.UserResponse;
import com.sahasathi.exception.BadRequestException;
import com.sahasathi.exception.ResourceNotFoundException;
import com.sahasathi.model.Gender;
import com.sahasathi.model.User;
import com.sahasathi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final FirebaseService firebaseService;

    @Transactional
    public UserResponse registerOrLogin(String idToken) {
        String phoneNumber = firebaseService.getPhoneNumberFromToken(idToken);
        String firebaseUid = firebaseService.verifyIdToken(idToken).getUid();

        return userRepository.findByPhoneNumber(phoneNumber)
                .map(user -> {
                    log.info("Existing user logged in: {}", phoneNumber);
                    if (!user.getFirebaseUid().equals(firebaseUid)) {
                        user.setFirebaseUid(firebaseUid);
                        userRepository.save(user);
                    }
                    return mapToResponse(user, false);
                })
                .orElseGet(() -> {
                    log.info("New user registering: {}", phoneNumber);
                    User newUser = User.builder()
                            .firebaseUid(firebaseUid)
                            .phoneNumber(phoneNumber)
                            .name("User")
                            .active(true)
                            .build();
                    User saved = userRepository.save(newUser);
                    return mapToResponse(saved, true);
                });
    }

    @Transactional
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setBio(request.getBio());
        user.setLocality(request.getLocality());
        user.setCity(request.getCity());
        user.setState(request.getState());
        user.setLatitude(request.getLatitude());
        user.setLongitude(request.getLongitude());

        if (request.getDateOfBirth() != null) {
            user.setDateOfBirth(request.getDateOfBirth());
            user.setAge(calculateAge(request.getDateOfBirth()));
        }

        if (request.getGender() != null) {
            try {
                user.setGender(Gender.valueOf(request.getGender().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid gender. Must be MALE, FEMALE, or OTHER");
            }
        }

        User saved = userRepository.save(user);
        log.info("Profile updated for user: {}", user.getPhoneNumber());
        return mapToResponse(saved, false);
    }

    public UserResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return mapToResponse(user, false);
    }

    private int calculateAge(LocalDate dateOfBirth) {
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }

    private UserResponse mapToResponse(User user, boolean isNewUser) {
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
                .isNewUser(isNewUser)
                .build();
    }
}
