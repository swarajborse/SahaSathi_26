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
public class CommunityListResponse {

    private Long id;
    private String name;
    private String category;
    private String locality;
    private String city;
    private int memberCount;
    private Integer maxMembers;
    private boolean isPrivate;
    private String creatorName;
    private boolean isJoined;
    private LocalDateTime createdAt;
}
