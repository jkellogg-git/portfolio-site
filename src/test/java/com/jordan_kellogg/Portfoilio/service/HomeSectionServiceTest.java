package com.jordan_kellogg.Portfoilio.service;

import com.jordan_kellogg.Portfoilio.dto.ExperienceForm;
import com.jordan_kellogg.Portfoilio.dto.HomeSectionForm;
import com.jordan_kellogg.Portfoilio.model.Experience;
import com.jordan_kellogg.Portfoilio.model.HomeSection;
import com.jordan_kellogg.Portfoilio.repository.HomeSectionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class HomeSectionServiceTest {

    @Mock
    private HomeSectionRepository homeSectionRepository;

    @InjectMocks
    private HomeSectionService homeSectionService;

    @Test
    void getHomeSection_shouldReturnSection_whenExists() {
        HomeSection section = new HomeSection();
        section.setHeroHeading("Hello");
        when(homeSectionRepository.findAll()).thenReturn(List.of(section));

        HomeSection result = homeSectionService.getHomeSection();

        assertThat(result).isNotNull();
        assertThat(result.getHeroHeading()).isEqualTo("Hello");
    }

    @Test
    void getHomeSection_shouldReturnNull_whenEmpty() {
        when(homeSectionRepository.findAll()).thenReturn(List.of());

        HomeSection result = homeSectionService.getHomeSection();

        assertThat(result).isNull();
    }

    @Test
    void getHomeSectionForm_shouldMapEntityToForm() {
        HomeSection section = new HomeSection();
        section.setId(1L);
        section.setHeroHeading("Heading");
        section.setHeroSubtitle("Subtitle");
        section.setProfilePhotoUrl("http://example.com/photo.jpg");
        section.setProjectsHeading("Projects");

        Experience exp = new Experience();
        exp.setId(10L);
        exp.setTitle("Engineer");
        exp.setStartDate("Jan 2022");
        exp.setEndDate("Present");
        exp.setDetails("Built things");
        section.setExperiences(new ArrayList<>(List.of(exp)));

        when(homeSectionRepository.findAll()).thenReturn(List.of(section));

        HomeSectionForm form = homeSectionService.getHomeSectionForm();

        assertThat(form.getId()).isEqualTo(1L);
        assertThat(form.getHeroHeading()).isEqualTo("Heading");
        assertThat(form.getHeroSubtitle()).isEqualTo("Subtitle");
        assertThat(form.getProfilePhotoUrl()).isEqualTo("http://example.com/photo.jpg");
        assertThat(form.getProjectsHeading()).isEqualTo("Projects");
        assertThat(form.getExperiences()).hasSize(1);
        assertThat(form.getExperiences().get(0).getTitle()).isEqualTo("Engineer");
        assertThat(form.getExperiences().get(0).getStartDate()).isEqualTo("Jan 2022");
        assertThat(form.getExperiences().get(0).getEndDate()).isEqualTo("Present");
        assertThat(form.getExperiences().get(0).getDetails()).isEqualTo("Built things");
    }

    @Test
    void getHomeSectionForm_shouldReturnEmptyForm_whenNoSectionExists() {
        when(homeSectionRepository.findAll()).thenReturn(List.of());

        HomeSectionForm form = homeSectionService.getHomeSectionForm();

        assertThat(form).isNotNull();
        assertThat(form.getId()).isNull();
        assertThat(form.getExperiences()).isEmpty();
    }

    @Test
    void save_shouldUpdateFieldsOnExistingSection() {
        HomeSection section = new HomeSection();
        section.setId(1L);
        section.setHeroHeading("Old");
        section.setExperiences(new ArrayList<>());

        HomeSectionForm form = new HomeSectionForm();
        form.setId(1L);
        form.setHeroHeading("New Heading");
        form.setHeroSubtitle("New Subtitle");
        form.setProfilePhotoUrl("http://example.com/new.jpg");
        form.setProjectsHeading("New Projects");
        form.setExperiences(new ArrayList<>());

        when(homeSectionRepository.findById(1L)).thenReturn(Optional.of(section));
        when(homeSectionRepository.save(any(HomeSection.class))).thenAnswer(inv -> inv.getArgument(0));

        HomeSection result = homeSectionService.save(form);

        assertThat(result.getHeroHeading()).isEqualTo("New Heading");
        assertThat(result.getHeroSubtitle()).isEqualTo("New Subtitle");
        assertThat(result.getProfilePhotoUrl()).isEqualTo("http://example.com/new.jpg");
        assertThat(result.getProjectsHeading()).isEqualTo("New Projects");
        verify(homeSectionRepository).save(section);
    }

    @Test
    void save_shouldAddExperiencesFromForm() {
        HomeSection section = new HomeSection();
        section.setId(1L);
        section.setExperiences(new ArrayList<>());

        ExperienceForm ef = new ExperienceForm();
        ef.setTitle("Developer");
        ef.setStartDate("Mar 2023");
        ef.setEndDate("Present");
        ef.setDetails("Wrote code");

        HomeSectionForm form = new HomeSectionForm();
        form.setId(1L);
        form.setExperiences(List.of(ef));

        when(homeSectionRepository.findById(1L)).thenReturn(Optional.of(section));
        when(homeSectionRepository.save(any(HomeSection.class))).thenAnswer(inv -> inv.getArgument(0));

        HomeSection result = homeSectionService.save(form);

        assertThat(result.getExperiences()).hasSize(1);
        assertThat(result.getExperiences().get(0).getTitle()).isEqualTo("Developer");
        assertThat(result.getExperiences().get(0).getHomeSection()).isEqualTo(section);
    }

    @Test
    void save_shouldSkipExperiencesWithBlankTitle() {
        HomeSection section = new HomeSection();
        section.setId(1L);
        section.setExperiences(new ArrayList<>());

        ExperienceForm filled = new ExperienceForm();
        filled.setTitle("Valid Role");

        ExperienceForm blank = new ExperienceForm();
        blank.setTitle("");

        ExperienceForm nullTitle = new ExperienceForm();
        nullTitle.setTitle(null);

        HomeSectionForm form = new HomeSectionForm();
        form.setId(1L);
        form.setExperiences(List.of(filled, blank, nullTitle));

        when(homeSectionRepository.findById(1L)).thenReturn(Optional.of(section));
        when(homeSectionRepository.save(any(HomeSection.class))).thenAnswer(inv -> inv.getArgument(0));

        HomeSection result = homeSectionService.save(form);

        assertThat(result.getExperiences()).hasSize(1);
        assertThat(result.getExperiences().get(0).getTitle()).isEqualTo("Valid Role");
    }

    @Test
    void save_shouldLimitExperiencesToThree() {
        HomeSection section = new HomeSection();
        section.setId(1L);
        section.setExperiences(new ArrayList<>());

        List<ExperienceForm> fourExperiences = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            ExperienceForm ef = new ExperienceForm();
            ef.setTitle("Role " + i);
            fourExperiences.add(ef);
        }

        HomeSectionForm form = new HomeSectionForm();
        form.setId(1L);
        form.setExperiences(fourExperiences);

        when(homeSectionRepository.findById(1L)).thenReturn(Optional.of(section));
        when(homeSectionRepository.save(any(HomeSection.class))).thenAnswer(inv -> inv.getArgument(0));

        HomeSection result = homeSectionService.save(form);

        assertThat(result.getExperiences()).hasSize(3);
    }

    @Test
    void save_shouldThrowException_whenSectionNotFound() {
        HomeSectionForm form = new HomeSectionForm();
        form.setId(999L);

        when(homeSectionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> homeSectionService.save(form))
                .isInstanceOf(IllegalArgumentException.class);
    }
}