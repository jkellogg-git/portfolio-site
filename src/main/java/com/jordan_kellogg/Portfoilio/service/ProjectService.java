package com.jordan_kellogg.Portfoilio.service;

import com.jordan_kellogg.Portfoilio.dto.ProjectForm;
import com.jordan_kellogg.Portfoilio.model.Project;
import com.jordan_kellogg.Portfoilio.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectService {

    private final ProjectRepository projectRepository;

    public List<Project> findAll() {
        return projectRepository.findAllByOrderByDateAddedDesc();
    }

    public Project findById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with id: " + id));
    }

    @Transactional
    public Project save(ProjectForm form) {
        Project project;
        if (form.getId() != null) {
            project = findById(form.getId());
        } else {
            project = new Project();
        }
        project.setTitle(form.getTitle());
        project.setDescription(form.getDescription());
        project.setThumbnailUrl(form.getThumbnailUrl());
        project.setLiveDemoUrl(form.getLiveDemoUrl());
        project.setSourceCodeUrl(form.getSourceCodeUrl());
        return projectRepository.save(project);
    }

    @Transactional
    public void delete(Long id) {
        projectRepository.deleteById(id);
    }
}
