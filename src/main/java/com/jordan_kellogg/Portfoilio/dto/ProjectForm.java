package com.jordan_kellogg.Portfoilio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectForm {

    private Long id;

    @NotBlank(message = "Title is required")
    @Size(max = 200)
    private String title;

    @Size(max = 2000, message = "Description must be 2000 characters or less")
    private String description;

    private String thumbnailUrl;

    private String liveDemoUrl;

    private String sourceCodeUrl;
}