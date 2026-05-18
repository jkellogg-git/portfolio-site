package com.jordan_kellogg.Portfoilio.service;

import com.jordan_kellogg.Portfoilio.dto.HomeSectionForm;
import com.jordan_kellogg.Portfoilio.model.HomeSection;
import com.jordan_kellogg.Portfoilio.repository.HomeSectionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HomeSectionServiceTest {

    @Mock
    private HomeSectionRepository homeSectionRepository;

    @InjectMocks
    private HomeSectionService homeSectionService;

    @Test
    void findAll_shouldReturnAllSections() {
        List<HomeSection> sections = List.of(new HomeSection(), new HomeSection());
        when(homeSectionRepository.findAll()).thenReturn(sections);

        List<HomeSection> result = homeSectionService.findAll();

        assertThat(result).hasSize(2);
        verify(homeSectionRepository).findAll();
    }

    @Test
    void findByKey_shouldReturnSection_whenExists() {
        HomeSection section = new HomeSection();
        section.setSectionKey("hero_heading");
        when(homeSectionRepository.findBySectionKey("hero_heading")).thenReturn(Optional.of(section));

        HomeSection result = homeSectionService.findByKey("hero_heading");

        assertThat(result.getSectionKey()).isEqualTo("hero_heading");
    }

    @Test
    void findByKey_shouldThrowException_whenNotExists() {
        when(homeSectionRepository.findBySectionKey("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> homeSectionService.findByKey("nonexistent"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void findAllAsMap_shouldReturnKeyContentMap() {
        HomeSection s1 = new HomeSection();
        s1.setSectionKey("hero_heading");
        s1.setContent("Heading");
        
        HomeSection s2 = new HomeSection();
        s2.setSectionKey("hero_subtitle");
        s2.setContent("Subtitle");

        when(homeSectionRepository.findAll()).thenReturn(List.of(s1, s2));

        Map<String, String> result = homeSectionService.findAllAsMap();

        assertThat(result).hasSize(2);
        assertThat(result.get("hero_heading")).isEqualTo("Heading");
        assertThat(result.get("hero_subtitle")).isEqualTo("Subtitle");
    }

    @Test
    void findAllAsMap_shouldReturnEmptyStringForNullContent() {
        HomeSection s1 = new HomeSection();
        s1.setSectionKey("hero_heading");
        s1.setContent(null);

        when(homeSectionRepository.findAll()).thenReturn(List.of(s1));

        Map<String, String> result = homeSectionService.findAllAsMap();

        assertThat(result.get("hero_heading")).isEqualTo("");
    }

    @Test
    void save_shouldUpdateContentOnExistingSection() {
        HomeSection section = new HomeSection();
        section.setId(1L);
        section.setSectionKey("key");
        section.setContent("old");

        HomeSectionForm form = new HomeSectionForm();
        form.setId(1L);
        form.setContent("new content");

        when(homeSectionRepository.findById(1L)).thenReturn(Optional.of(section));
        when(homeSectionRepository.save(any(HomeSection.class))).thenAnswer(invocation -> invocation.getArgument(0));

        HomeSection result = homeSectionService.save(form);

        assertThat(result.getContent()).isEqualTo("new content");
        verify(homeSectionRepository).save(section);
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
