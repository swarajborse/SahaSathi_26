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
public class SearchResult {

    private String type;
    private Long id;
    private String title;
    private String subtitle;
    private String imageUrl;
    private String city;
    private String locality;
    private String category;
    private LocalDateTime dateTime;
    private Integer participantCount;
    private Integer memberCount;
    private boolean isPrivate;
}
