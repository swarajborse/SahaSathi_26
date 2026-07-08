package com.sahasathi.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
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
public class CreateActivityRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must be at most 200 characters")
    private String title;

    @Size(max = 2000, message = "Description must be at most 2000 characters")
    private String description;

    @NotBlank(message = "Category is required")
    private String category;

    @NotNull(message = "Date and time is required")
    @Future(message = "Date and time must be in the future")
    private LocalDateTime dateTime;

    @Positive(message = "Duration must be positive")
    private Integer duration;

    @NotBlank(message = "Location is required")
    @Size(max = 500, message = "Location must be at most 500 characters")
    private String location;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City must be at most 100 characters")
    private String city;

    @Size(max = 200, message = "Locality must be at most 200 characters")
    private String locality;

    private Double latitude;

    private Double longitude;

    @Positive(message = "Max participants must be positive")
    private Integer maxParticipants;

    private boolean isPrivate;

    @Positive(message = "Min age must be positive")
    private Integer minAge;
}
