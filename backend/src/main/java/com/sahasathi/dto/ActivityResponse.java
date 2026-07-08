package com.sahasathi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityResponse {

    private Long id;
    private String title;
    private String description;
    private String category;
    private LocalDateTime dateTime;
    private Integer duration;
    private String location;
    private String locality;
    private String city;
    private Double latitude;
    private Double longitude;
    private Integer maxParticipants;
    private int participantCount;
    private String status;
    private boolean isPrivate;
    private Integer minAge;
    private String imageUrl;
    private boolean isJoined;
    private boolean isCreator;
    private UserSummary creator;
    private List<UserSummary> participants;
    private LocalDateTime createdAt;
}


