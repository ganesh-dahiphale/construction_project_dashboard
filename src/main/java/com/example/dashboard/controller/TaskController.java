package com.example.dashboard.controller;

import com.example.dashboard.model.Task;
import com.example.dashboard.model.TaskStatus;
import com.example.dashboard.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public String listTasks(Model model) {
        model.addAttribute("tasks", taskService.getAllTasks());
        return "tasks/list";
    }

    @GetMapping("/new")
    public String newTaskForm(Model model) {
        if (!model.containsAttribute("task")) {
            model.addAttribute("task", new Task());
        }
        populateFormAttributes(model);
        return "tasks/form";
    }

    @PostMapping
    public String createTask(
            @Valid @ModelAttribute("task") Task task,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            populateFormAttributes(model);
            return "tasks/form";
        }

        taskService.createTask(task);
        redirectAttributes.addFlashAttribute("successMessage", "Task '" + task.getTitle() + "' successfully recorded!");
        return "redirect:/tasks";
    }

    @GetMapping("/{id}/edit")
    public String editTaskForm(@org.springframework.web.bind.annotation.PathVariable("id") Long id, Model model) {
        Task task = taskService.getTaskById(id)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Task not found with ID: " + id));

        if (!model.containsAttribute("taskForm")) {
            model.addAttribute("taskForm", new com.example.dashboard.dto.TaskUpdateDto(
                    task.getStatus(),
                    task.getPercentComplete(),
                    task.getRemarks()
            ));
        }
        model.addAttribute("task", task);
        model.addAttribute("statuses", TaskStatus.values());
        model.addAttribute("activeTab", "tasks");
        return "tasks/edit";
    }

    @PostMapping("/{id}/edit")
    public String updateTask(
            @org.springframework.web.bind.annotation.PathVariable("id") Long id,
            @Valid @ModelAttribute("taskForm") com.example.dashboard.dto.TaskUpdateDto form,
            BindingResult bindingResult,
            java.security.Principal principal,
            Model model,
            RedirectAttributes redirectAttributes) {

        Task task = taskService.getTaskById(id)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Task not found with ID: " + id));

        if (bindingResult.hasErrors()) {
            model.addAttribute("task", task);
            model.addAttribute("statuses", TaskStatus.values());
            model.addAttribute("activeTab", "tasks");
            return "tasks/edit";
        }

        String updatedBy = (principal != null) ? principal.getName() : "system";
        taskService.updateTask(id, form.getStatus(), form.getPercentComplete(), form.getRemarks(), updatedBy);
        redirectAttributes.addFlashAttribute("successMessage", "Task #" + id + " updated successfully!");
        return "redirect:/tasks";
    }

    private void populateFormAttributes(Model model) {
        model.addAttribute("projects", taskService.getAllProjects());
        model.addAttribute("statuses", TaskStatus.values());
        model.addAttribute("activeTab", "tasks");
    }
}
