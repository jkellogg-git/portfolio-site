package com.jordan_kellogg.Portfoilio.repository;

import com.jordan_kellogg.Portfoilio.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findAllByOrderByDateAddedDesc();
}
