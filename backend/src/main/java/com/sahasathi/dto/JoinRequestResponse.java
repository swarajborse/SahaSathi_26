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
public class JoinRequestResponse {

    private Long id;
    private String targetType;
    private Long targetId;
    private String targetName;
    private UserSummary requester;
    private String status;
    private LocalDateTime createdAt;
}
