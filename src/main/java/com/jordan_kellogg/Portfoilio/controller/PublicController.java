package com.jordan_kellogg.Portfoilio.controller;

import com.jordan_kellogg.Portfoilio.model.Project;
import com.jordan_kellogg.Portfoilio.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class PublicController {

    private final ProjectService projectService;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("projects", projectService.findAll());
        return "public/home";
    }

    @GetMapping("/projects/{id}")
    public String projectDetail(@PathVariable Long id, Model model) {
        Project project = projectService.findById(id);
        model.addAttribute("project", project);
        return "public/project-detail";
    }
}
