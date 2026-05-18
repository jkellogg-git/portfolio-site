package com.jordan_kellogg.Portfoilio.repository;

import com.jordan_kellogg.Portfoilio.model.Experience;
import com.jordan_kellogg.Portfoilio.model.HomeSection;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
public class HomeSectionRepositoryTest {

    @Autowired
    private HomeSectionRepository homeSectionRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void save_shouldPersistHomeSectionAndGenerateId() {
        HomeSection section = new HomeSection();
        section.setHeroHeading("Hello World");
        section.setHeroSubtitle("A subtitle");
        section.setProfilePhotoUrl("http://example.com/photo.jpg");
        section.setProjectsHeading("My Projects");

        HomeSection saved = homeSectionRepository.save(section);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getHeroHeading()).isEqualTo("Hello World");
        assertThat(saved.getHeroSubtitle()).isEqualTo("A subtitle");
        assertThat(saved.getProfilePhotoUrl()).isEqualTo("http://example.com/photo.jpg");
        assertThat(saved.getProjectsHeading()).isEqualTo("My Projects");
    }

    @Test
    void save_shouldAutoSetUpdatedAt() {
        HomeSection section = new HomeSection();
        section.setHeroHeading("Test");

        HomeSection saved = homeSectionRepository.save(section);
        entityManager.flush();
        entityManager.clear();

        HomeSection found = homeSectionRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getUpdatedAt()).isNotNull();
    }

    @Test
    void findById_shouldReturnSection_whenExists() {
        HomeSection section = new HomeSection();
        section.setHeroHeading("Welcome");
        section.setProjectsHeading("Projects");
        HomeSection persisted = entityManager.persistAndFlush(section);
        entityManager.clear();

        Optional<HomeSection> found = homeSectionRepository.findById(persisted.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getHeroHeading()).isEqualTo("Welcome");
    }

    @Test
    void findById_shouldReturnEmpty_whenNotExists() {
        Optional<HomeSection> found = homeSectionRepository.findById(999L);
        assertThat(found).isEmpty();
    }

    @Test
    void findAll_shouldReturnAllSections() {
        HomeSection s1 = new HomeSection();
        s1.setHeroHeading("Section 1");
        HomeSection s2 = new HomeSection();
        s2.setHeroHeading("Section 2");

        entityManager.persist(s1);
        entityManager.persist(s2);
        entityManager.flush();
        entityManager.clear();

        List<HomeSection> sections = homeSectionRepository.findAll();

        assertThat(sections).hasSize(2);
    }

    @Test
    void update_shouldModifyFieldsAndUpdateTimestamp() {
        HomeSection section = new HomeSection();
        section.setHeroHeading("Old Heading");
        section.setHeroSubtitle("Old Subtitle");
        HomeSection saved = entityManager.persistAndFlush(section);
        LocalDateTime firstUpdate = saved.getUpdatedAt();
        entityManager.clear();

        HomeSection toUpdate = homeSectionRepository.findById(saved.getId()).orElseThrow();
        toUpdate.setHeroHeading("New Heading");
        toUpdate.setHeroSubtitle("New Subtitle");
        homeSectionRepository.saveAndFlush(toUpdate);
        entityManager.clear();

        HomeSection found = homeSectionRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getHeroHeading()).isEqualTo("New Heading");
        assertThat(found.getHeroSubtitle()).isEqualTo("New Subtitle");
        assertThat(found.getUpdatedAt()).isAfterOrEqualTo(firstUpdate);
    }

    @Test
    void delete_shouldRemoveSection() {
        HomeSection section = new HomeSection();
        section.setHeroHeading("To Delete");
        HomeSection persisted = entityManager.persistAndFlush(section);
        entityManager.clear();

        homeSectionRepository.delete(persisted);
        entityManager.flush();

        Optional<HomeSection> found = homeSectionRepository.findById(persisted.getId());
        assertThat(found).isEmpty();
    }

    @Test
    void save_shouldCascadePersistExperiences() {
        HomeSection section = new HomeSection();
        section.setHeroHeading("With Experiences");

        Experience exp = new Experience();
        exp.setTitle("Software Engineer");
        exp.setStartDate("Jan 2022");
        exp.setEndDate("Present");
        exp.setDetails("Built cool stuff");
        section.addExperience(exp);

        HomeSection saved = homeSectionRepository.save(section);
        entityManager.flush();
        entityManager.clear();

        HomeSection found = homeSectionRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getExperiences()).hasSize(1);
        assertThat(found.getExperiences().get(0).getTitle()).isEqualTo("Software Engineer");
        assertThat(found.getExperiences().get(0).getStartDate()).isEqualTo("Jan 2022");
        assertThat(found.getExperiences().get(0).getEndDate()).isEqualTo("Present");
        assertThat(found.getExperiences().get(0).getDetails()).isEqualTo("Built cool stuff");
    }

    @Test
    void orphanRemoval_shouldDeleteExperienceWhenRemovedFromList() {
        HomeSection section = new HomeSection();
        section.setHeroHeading("Test Orphan");

        Experience exp = new Experience();
        exp.setTitle("To Remove");
        section.addExperience(exp);

        HomeSection saved = homeSectionRepository.save(section);
        entityManager.flush();
        entityManager.clear();

        HomeSection toUpdate = homeSectionRepository.findById(saved.getId()).orElseThrow();
        assertThat(toUpdate.getExperiences()).hasSize(1);

        toUpdate.getExperiences().clear();
        homeSectionRepository.saveAndFlush(toUpdate);
        entityManager.clear();

        HomeSection found = homeSectionRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getExperiences()).isEmpty();
    }
}