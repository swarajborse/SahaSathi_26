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
public class CommunityResponse {

    private Long id;
    private String name;
    private String description;
    private String category;
    private String locality;
    private String city;
    private String coverImageUrl;
    private Integer maxMembers;
    private int memberCount;
    private boolean isPrivate;
    private Integer minAge;
    private boolean isJoined;
    private boolean isOrganizer;
    private UserSummary creator;
    private List<UserSummary> members;
    private LocalDateTime createdAt;
}
