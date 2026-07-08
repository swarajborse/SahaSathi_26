package com.sahasathi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityListResponse {

    private Long id;
    private String title;
    private String category;
    private LocalDateTime dateTime;
    private Integer duration;
    private String location;
    private String locality;
    private String city;
    private Integer maxParticipants;
    private int participantCount;
    private String status;
    private boolean isPrivate;
    private String creatorName;
    private String creatorProfilePicture;
    private boolean isJoined;
}
