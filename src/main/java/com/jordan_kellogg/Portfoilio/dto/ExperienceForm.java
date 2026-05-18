package com.jordan_kellogg.Portfoilio.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExperienceForm {

    private Long id;

    @Size(max = 200, message = "Title must be 200 characters or less")
    private String title;

    @Size(max = 50, message = "Start date must be 50 characters or less")
    private String startDate;

    @Size(max = 50, message = "End date must be 50 characters or less")
    private String endDate;

    @Size(max = 2000, message = "Details must be 2000 characters or less")
    private String details;
}
