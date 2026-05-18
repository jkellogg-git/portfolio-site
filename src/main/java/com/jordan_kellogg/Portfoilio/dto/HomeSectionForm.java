package com.jordan_kellogg.Portfoilio.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HomeSectionForm {

    @NotNull
    private Long id;

    @Size(max = 500, message = "Hero heading must be 500 characters or less")
    private String heroHeading;

    @Size(max = 1000, message = "Hero subtitle must be 1000 characters or less")
    private String heroSubtitle;

    @Size(max = 500, message = "Profile photo URL must be 500 characters or less")
    private String profilePhotoUrl;

    @Size(max = 500, message = "Projects heading must be 500 characters or less")
    private String projectsHeading;

    @Valid
    private List<ExperienceForm> experiences = new ArrayList<>();
}
