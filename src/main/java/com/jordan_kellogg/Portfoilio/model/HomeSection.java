package com.jordan_kellogg.Portfoilio.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "home_sections")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HomeSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hero_heading", length = 500)
    private String heroHeading;

    @Column(name = "hero_subtitle", length = 1000)
    private String heroSubtitle;

    @Column(name = "profile_photo_url", length = 500)
    private String profilePhotoUrl;

    @Column(name = "projects_heading", length = 500)
    private String projectsHeading;

    @OneToMany(mappedBy = "homeSection", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Experience> experiences = new ArrayList<>();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Helper method to manage the bidirectional relationship
    public void addExperience(Experience experience) {
        experiences.add(experience);
        experience.setHomeSection(this);
    }

    public void removeExperience(Experience experience) {
        experiences.remove(experience);
        experience.setHomeSection(null);
    }
}
