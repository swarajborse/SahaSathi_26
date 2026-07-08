package com.sahasathi.service;

import com.sahasathi.exception.BadRequestException;
import com.sahasathi.exception.ResourceNotFoundException;
import com.sahasathi.model.User;
import com.sahasathi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Period;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgeVerificationService {

    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    @Transactional
    public VerificationResult verifyAge(Long userId, MultipartFile aadhaarImage, LocalDate confirmedDob) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (user.isAgeVerified()) {
            return VerificationResult.alreadyVerified();
        }

        if (aadhaarImage == null || aadhaarImage.isEmpty()) {
            throw new BadRequestException("Aadhaar image is required");
        }

        validateImageType(aadhaarImage);

        String tempFilename = fileStorageService.storeFile(aadhaarImage);

        try {
            LocalDate dob = confirmedDob != null ? confirmedDob : user.getDateOfBirth();
            if (dob == null) {
                throw new BadRequestException(
                        "Date of birth is required. Please set your DOB in profile first.");
            }

            int age = Period.between(dob, LocalDate.now()).getYears();

            if (age >= 55) {
                user.setAgeVerified(true);
                user.setVerificationDate(LocalDate.now());
                if (user.getDateOfBirth() == null) {
                    user.setDateOfBirth(dob);
                    user.setAge(age);
                }
                userRepository.save(user);
                log.info("Age verified for user: {}, age: {}", userId, age);
                return VerificationResult.verified(age);
            } else {
                log.warn("Age verification failed for user: {}, age: {}", userId, age);
                return VerificationResult.tooYoung(age);
            }
        } finally {
            deleteTempFile(tempFilename);
        }
    }

    public boolean isAgeVerified(Long userId) {
        return userRepository.findById(userId)
                .map(User::isAgeVerified)
                .orElse(false);
    }

    private void validateImageType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BadRequestException("Only image files are accepted");
        }
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new BadRequestException("Image size must be less than 10MB");
        }
    }

    private void deleteTempFile(String filename) {
        try {
            if (filename != null) {
                Path filePath = Path.of(fileStorageService.getUploadDir()).resolve(filename);
                Files.deleteIfExists(filePath);
                log.debug("Temporary Aadhaar file deleted: {}", filename);
            }
        } catch (IOException e) {
            log.warn("Failed to delete temporary file: {}", filename);
        }
    }

    public record VerificationResult(boolean success, String message, int age) {
        static VerificationResult alreadyVerified() {
            return new VerificationResult(true, "Already verified", 0);
        }
        static VerificationResult verified(int age) {
            return new VerificationResult(true, "Age verified successfully. You are " + age + " years old.", age);
        }
        static VerificationResult tooYoung(int age) {
            return new VerificationResult(false,
                    "Age verification failed. You are " + age + " years old. Minimum age is 55.", age);
        }
    }
}
