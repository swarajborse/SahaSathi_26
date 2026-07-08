package com.sahasathi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NearbyUserResponse {

    private Long id;
    private String name;
    private Integer age;
    private String gender;
    private String locality;
    private String city;
    private String profilePictureUrl;
    private String bio;
    private List<InterestResponse> mutualInterests;
    private int mutualInterestCount;
}
