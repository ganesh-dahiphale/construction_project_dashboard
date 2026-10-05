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

    private void populateFormAttributes(Model model) {
        model.addAttribute("projects", taskService.getAllProjects());
        model.addAttribute("statuses", TaskStatus.values());
    }
}
