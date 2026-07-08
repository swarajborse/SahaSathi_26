package com.sahasathi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileCompletenessResponse {

    private int percentage;
    private boolean nameFilled;
    private boolean dobFilled;
    private boolean genderFilled;
    private boolean localityFilled;
    private boolean cityFilled;
    private boolean bioFilled;
    private boolean hasInterests;
    private boolean hasProfilePicture;
}
