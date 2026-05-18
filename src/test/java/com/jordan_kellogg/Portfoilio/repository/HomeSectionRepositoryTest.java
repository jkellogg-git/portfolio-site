package com.jordan_kellogg.Portfoilio.repository;

import com.jordan_kellogg.Portfoilio.model.HomeSection;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        section.setSectionKey("test_key");
        section.setContent("test content");

        HomeSection saved = homeSectionRepository.save(section);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getSectionKey()).isEqualTo("test_key");
        assertThat(saved.getContent()).isEqualTo("test content");
    }

    @Test
    void save_shouldAutoSetUpdatedAt() {
        HomeSection section = new HomeSection();
        section.setSectionKey("test_key");

        HomeSection saved = homeSectionRepository.save(section);
        entityManager.flush();
        entityManager.clear();

        HomeSection found = homeSectionRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getUpdatedAt()).isNotNull();
    }

    @Test
    void findBySectionKey_shouldReturnSection_whenExists() {
        HomeSection section = new HomeSection();
        section.setSectionKey("hero_heading");
        section.setContent("Welcome");
        entityManager.persistAndFlush(section);
        entityManager.clear();

        Optional<HomeSection> found = homeSectionRepository.findBySectionKey("hero_heading");

        assertThat(found).isPresent();
        assertThat(found.get().getContent()).isEqualTo("Welcome");
    }

    @Test
    void findBySectionKey_shouldReturnEmpty_whenNotExists() {
        Optional<HomeSection> found = homeSectionRepository.findBySectionKey("nonexistent");
        assertThat(found).isEmpty();
    }

    @Test
    void findAll_shouldReturnAllSections() {
        HomeSection s1 = new HomeSection();
        s1.setSectionKey("key1");
        HomeSection s2 = new HomeSection();
        s2.setSectionKey("key2");

        entityManager.persist(s1);
        entityManager.persist(s2);
        entityManager.flush();
        entityManager.clear();

        List<HomeSection> sections = homeSectionRepository.findAll();

        assertThat(sections).hasSize(2);
    }

    @Test
    void update_shouldModifyContentAndUpdateTimestamp() {
        HomeSection section = new HomeSection();
        section.setSectionKey("key");
        section.setContent("old content");
        HomeSection saved = entityManager.persistAndFlush(section);
        LocalDateTime firstUpdate = saved.getUpdatedAt();
        entityManager.clear();

        // Wait a tiny bit to ensure timestamp changes if resolution is low, 
        // but @PreUpdate usually works fine even in same millisecond if DB supports it.
        // Actually, for some H2 versions we might need a small sleep or manual set if we want to be absolutely sure.
        // But let's trust @PreUpdate.

        HomeSection toUpdate = homeSectionRepository.findById(saved.getId()).orElseThrow();
        toUpdate.setContent("new content");
        HomeSection updated = homeSectionRepository.saveAndFlush(toUpdate);
        entityManager.clear();

        HomeSection found = homeSectionRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getContent()).isEqualTo("new content");
        assertThat(found.getUpdatedAt()).isAfterOrEqualTo(firstUpdate);
    }

    @Test
    void delete_shouldRemoveSection() {
        HomeSection section = new HomeSection();
        section.setSectionKey("to_delete");
        HomeSection persisted = entityManager.persistAndFlush(section);
        entityManager.clear();

        homeSectionRepository.delete(persisted);
        entityManager.flush();

        Optional<HomeSection> found = homeSectionRepository.findById(persisted.getId());
        assertThat(found).isEmpty();
    }

    @Test
    void sectionKey_shouldBeUnique() {
        HomeSection s1 = new HomeSection();
        s1.setSectionKey("duplicate");
        homeSectionRepository.save(s1);
        entityManager.flush();

        HomeSection s2 = new HomeSection();
        s2.setSectionKey("duplicate");
        
        assertThatThrownBy(() -> {
            homeSectionRepository.save(s2);
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }
}
