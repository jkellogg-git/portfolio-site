package com.jordan_kellogg.Portfoilio.repository;

import com.jordan_kellogg.Portfoilio.model.Project;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
public class ProjectRepositoryTest {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void save_shouldPersistProjectAndGenerateId() {
        Project project = Project.builder()
                .title("Test Project")
                .description("Test Description")
                .thumbnailUrl("http://example.com/thumb.png")
                .liveDemoUrl("http://example.com/demo")
                .sourceCodeUrl("http://example.com/source")
                .build();

        Project savedProject = projectRepository.save(project);

        assertThat(savedProject.getId()).isNotNull();
        assertThat(savedProject.getTitle()).isEqualTo("Test Project");
        assertThat(savedProject.getDescription()).isEqualTo("Test Description");
        assertThat(savedProject.getDateAdded()).isNotNull();
    }

    @Test
    void save_shouldAutoSetDateAdded_whenNull() {
        Project project = Project.builder()
                .title("Test Project")
                .build();

        Project savedProject = projectRepository.save(project);
        entityManager.flush();
        entityManager.clear();

        Project foundProject = projectRepository.findById(savedProject.getId()).orElseThrow();
        assertThat(foundProject.getDateAdded()).isEqualTo(LocalDate.now());
    }

    @Test
    void findById_shouldReturnProject_whenExists() {
        Project project = Project.builder()
                .title("Test Project")
                .dateAdded(LocalDate.now())
                .build();
        Project persisted = entityManager.persistAndFlush(project);
        entityManager.clear();

        Optional<Project> found = projectRepository.findById(persisted.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Test Project");
    }

    @Test
    void findById_shouldReturnEmpty_whenNotExists() {
        Optional<Project> found = projectRepository.findById(999L);
        assertThat(found).isEmpty();
    }

    @Test
    void findAllByOrderByDateAddedDesc_shouldReturnProjectsInDescOrder() {
        Project p1 = Project.builder().title("Oldest").dateAdded(LocalDate.of(2024, 1, 1)).build();
        Project p2 = Project.builder().title("Middle").dateAdded(LocalDate.of(2024, 6, 15)).build();
        Project p3 = Project.builder().title("Newest").dateAdded(LocalDate.of(2024, 12, 25)).build();

        entityManager.persist(p1);
        entityManager.persist(p2);
        entityManager.persist(p3);
        entityManager.flush();
        entityManager.clear();

        List<Project> projects = projectRepository.findAllByOrderByDateAddedDesc();

        assertThat(projects).hasSize(3);
        assertThat(projects.get(0).getTitle()).isEqualTo("Newest");
        assertThat(projects.get(1).getTitle()).isEqualTo("Middle");
        assertThat(projects.get(2).getTitle()).isEqualTo("Oldest");
    }

    @Test
    void findAllByOrderByDateAddedDesc_shouldReturnEmptyList_whenNoProjects() {
        List<Project> projects = projectRepository.findAllByOrderByDateAddedDesc();
        assertThat(projects).isEmpty();
    }

    @Test
    void delete_shouldRemoveProject() {
        Project project = Project.builder().title("To Delete").dateAdded(LocalDate.now()).build();
        Project persisted = entityManager.persistAndFlush(project);
        entityManager.clear();

        projectRepository.delete(persisted);
        entityManager.flush();

        Optional<Project> found = projectRepository.findById(persisted.getId());
        assertThat(found).isEmpty();
    }

    @Test
    void update_shouldModifyExistingProject() {
        Project project = Project.builder().title("Original Title").dateAdded(LocalDate.now()).build();
        Project persisted = entityManager.persistAndFlush(project);
        entityManager.clear();

        Project toUpdate = projectRepository.findById(persisted.getId()).orElseThrow();
        toUpdate.setTitle("Updated Title");
        projectRepository.save(toUpdate);
        entityManager.flush();
        entityManager.clear();

        Project updated = projectRepository.findById(persisted.getId()).orElseThrow();
        assertThat(updated.getTitle()).isEqualTo("Updated Title");
    }
}
