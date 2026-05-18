package com.jordan_kellogg.Portfoilio.service;

import com.jordan_kellogg.Portfoilio.dto.ExperienceForm;
import com.jordan_kellogg.Portfoilio.dto.HomeSectionForm;
import com.jordan_kellogg.Portfoilio.model.Experience;
import com.jordan_kellogg.Portfoilio.model.HomeSection;
import com.jordan_kellogg.Portfoilio.repository.HomeSectionRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeSectionService {

    private final HomeSectionRepository homeSectionRepository;

    /**
     * Returns the singleton HomeSection (first row), or null if not yet seeded.
     */
    public HomeSection getHomeSection() {
        return homeSectionRepository.findAll()
                .stream()
                .findFirst()
                .orElse(null);
    }

    /**
     * Builds a HomeSectionForm from the current entity for the admin edit page.
     */
    public HomeSectionForm getHomeSectionForm() {
        HomeSection section = getHomeSection();
        if (section == null) {
            return new HomeSectionForm();
        }

        HomeSectionForm form = new HomeSectionForm();
        form.setId(section.getId());
        form.setHeroHeading(section.getHeroHeading());
        form.setHeroSubtitle(section.getHeroSubtitle());
        form.setProfilePhotoUrl(section.getProfilePhotoUrl());
        form.setProjectsHeading(section.getProjectsHeading());

        List<ExperienceForm> expForms = new ArrayList<>();
        for (Experience exp : section.getExperiences()) {
            ExperienceForm ef = new ExperienceForm();
            ef.setId(exp.getId());
            ef.setTitle(exp.getTitle());
            ef.setStartDate(exp.getStartDate());
            ef.setEndDate(exp.getEndDate());
            ef.setDetails(exp.getDetails());
            expForms.add(ef);
        }
        form.setExperiences(expForms);

        return form;
    }

    /**
     * Saves the home section content and syncs experiences (max 3).
     */
    @Transactional
    public HomeSection save(HomeSectionForm form) {
        HomeSection section = homeSectionRepository.findById(form.getId())
                .orElseThrow(() -> new IllegalArgumentException("Home section not found with id: " + form.getId()));

        section.setHeroHeading(form.getHeroHeading());
        section.setHeroSubtitle(form.getHeroSubtitle());
        section.setProfilePhotoUrl(form.getProfilePhotoUrl());
        section.setProjectsHeading(form.getProjectsHeading());

        // Clear existing experiences and rebuild from form (orphanRemoval handles deletes)
        section.getExperiences().clear();

        if (form.getExperiences() != null) {
            List<ExperienceForm> experienceForms = form.getExperiences().stream()
                    .filter(ef -> ef.getTitle() != null && !ef.getTitle().isBlank())
                    .limit(3)
                    .toList();

            for (ExperienceForm ef : experienceForms) {
                Experience exp = new Experience();
                exp.setTitle(ef.getTitle());
                exp.setStartDate(ef.getStartDate());
                exp.setEndDate(ef.getEndDate());
                exp.setDetails(ef.getDetails());
                section.addExperience(exp);
            }
        }

        return homeSectionRepository.save(section);
    }

    /**
     * Seeds default values on first startup if the table is empty.
     */
    @PostConstruct
    @Transactional
    public void seedDefaults() {
        if (homeSectionRepository.count() == 0) {
            HomeSection section = new HomeSection();
            section.setHeroHeading("Hi, I'm Jordan Kellogg");
            section.setHeroSubtitle("Software Developer — Check out my recent projects below.");
            section.setProfilePhotoUrl("");
            section.setProjectsHeading("Projects");
            homeSectionRepository.save(section);
        }
    }
}
