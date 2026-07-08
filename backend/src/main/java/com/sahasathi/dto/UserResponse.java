package com.sahasathi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String phoneNumber;
    private String name;
    private String email;
    private LocalDate dateOfBirth;
    private Integer age;
    private String gender;
    private String bio;
    private String locality;
    private String city;
    private String state;
    private Double latitude;
    private Double longitude;
    private String profilePictureUrl;
    private boolean ageVerified;
    private boolean active;
    private LocalDateTime createdAt;
    private boolean isNewUser;
}
