package com.jordan_kellogg.Portfoilio.controller;

import com.jordan_kellogg.Portfoilio.dto.ExperienceForm;
import com.jordan_kellogg.Portfoilio.dto.HomeSectionForm;
import com.jordan_kellogg.Portfoilio.dto.ProjectForm;
import com.jordan_kellogg.Portfoilio.model.Project;
import com.jordan_kellogg.Portfoilio.service.HomeSectionService;
import com.jordan_kellogg.Portfoilio.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ProjectService projectService;
    private final HomeSectionService homeSectionService;

    @GetMapping("/login")
    public String loginPage() {
        return "admin/login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("projects", projectService.findAll());
        return "admin/dashboard";
    }

    @GetMapping("/projects/new")
    public String newProjectForm(Model model) {
        model.addAttribute("projectForm", new ProjectForm());
        return "admin/project-form";
    }

    @GetMapping("/projects/{id}/edit")
    public String editProjectForm(@PathVariable Long id, Model model) {
        Project project = projectService.findById(id);
        ProjectForm form = new ProjectForm();
        form.setId(project.getId());
        form.setTitle(project.getTitle());
        form.setDescription(project.getDescription());
        form.setThumbnailUrl(project.getThumbnailUrl());
        form.setLiveDemoUrl(project.getLiveDemoUrl());
        form.setSourceCodeUrl(project.getSourceCodeUrl());
        model.addAttribute("projectForm", form);
        return "admin/project-form";
    }

    @PostMapping("/projects/save")
    public String saveProject(
            @Valid @ModelAttribute("projectForm") ProjectForm form,
            BindingResult result,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/project-form";
        }
        projectService.save(form);
        redirectAttributes.addFlashAttribute("successMessage", "Project saved!");
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/projects/{id}/delete")
    public String deleteProject(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        projectService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Project deleted.");
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/home-sections")
    public String homeSections(Model model) {
        HomeSectionForm form = homeSectionService.getHomeSectionForm();

        // Pad to 3 experience slots so the form always shows 3 inputs
        while (form.getExperiences().size() < 3) {
            form.getExperiences().add(new ExperienceForm());
        }

        model.addAttribute("homeSectionForm", form);
        return "admin/home-sections";
    }

    @PostMapping("/home-sections/save")
    public String saveHomeSection(
            @Valid @ModelAttribute("homeSectionForm") HomeSectionForm form,
            BindingResult result,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/home-sections";
        }
        homeSectionService.save(form);
        redirectAttributes.addFlashAttribute("successMessage", "Home content updated!");
        return "redirect:/admin/home-sections";
    }
}