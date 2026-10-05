package com.example.dashboard.controller;

import com.example.dashboard.model.Project;
import com.example.dashboard.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/projects")
public class AdminProjectController {

    private final ProjectService projectService;

    public AdminProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public String listProjects(Model model) {
        model.addAttribute("projects", projectService.getAllProjects());
        model.addAttribute("activeTab", "admin");
        return "admin/projects/list";
    }

    @GetMapping("/new")
    public String newProjectForm(Model model) {
        if (!model.containsAttribute("project")) {
            model.addAttribute("project", new Project());
        }
        model.addAttribute("activeTab", "admin");
        return "admin/projects/form";
    }

    @PostMapping
    public String createProject(
            @Valid @ModelAttribute("project") Project project,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("activeTab", "admin");
            return "admin/projects/form";
        }

        projectService.createProject(project);
        redirectAttributes.addFlashAttribute("successMessage", "Project '" + project.getName() + "' created successfully!");
        return "redirect:/admin/projects";
    }

    @GetMapping("/{id}/edit")
    public String editProjectForm(@PathVariable("id") Long id, Model model) {
        Project project = projectService.getProjectById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found with ID: " + id));

        model.addAttribute("project", project);
        model.addAttribute("activeTab", "admin");
        return "admin/projects/form";
    }

    @PostMapping("/{id}/edit")
    public String updateProject(
            @PathVariable("id") Long id,
            @Valid @ModelAttribute("project") Project project,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("activeTab", "admin");
            return "admin/projects/form";
        }

        projectService.updateProject(id, project);
        redirectAttributes.addFlashAttribute("successMessage", "Project '" + project.getName() + "' updated successfully!");
        return "redirect:/admin/projects";
    }
}
