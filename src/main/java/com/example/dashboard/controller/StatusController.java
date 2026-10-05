package com.example.dashboard.controller;

import com.example.dashboard.model.Task;
import com.example.dashboard.model.TaskStatus;
import com.example.dashboard.service.AlertService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/status")
public class StatusController {

    private final AlertService alertService;

    public StatusController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping
    public String showStatusIndex(Model model) {
        model.addAttribute("notStartedCount", alertService.getTasksByStatus(TaskStatus.NOT_STARTED).size());
        model.addAttribute("inProgressCount", alertService.getTasksByStatus(TaskStatus.IN_PROGRESS).size());
        model.addAttribute("completedCount", alertService.getTasksByStatus(TaskStatus.COMPLETED).size());
        model.addAttribute("blockedCount", alertService.getTasksByStatus(TaskStatus.BLOCKED).size());
        model.addAttribute("delayedCount", alertService.getDelayedTasks().size());
        model.addAttribute("activeTab", "status");
        return "status/index";
    }

    @GetMapping("/{status}")
    public String showStatusDrilldown(@PathVariable("status") String status, Model model) {
        String normalized = status.trim().toUpperCase();
        List<Task> tasks;
        String heading;
        String badgeClass;

        if ("DELAYED".equals(normalized)) {
            tasks = alertService.getDelayedTasks();
            heading = "Delayed Tasks (Past Due Date)";
            badgeClass = "bg-danger";
        } else if ("PLANNED".equals(normalized) || "NOT_STARTED".equals(normalized)) {
            tasks = alertService.getTasksByStatus(TaskStatus.NOT_STARTED);
            heading = "Planned / Not Started Tasks";
            badgeClass = "bg-secondary";
        } else {
            try {
                TaskStatus taskStatus = TaskStatus.valueOf(normalized);
                tasks = alertService.getTasksByStatus(taskStatus);
                heading = taskStatus.getDisplayName() + " Tasks";
                badgeClass = switch (taskStatus) {
                    case IN_PROGRESS -> "bg-primary";
                    case BLOCKED -> "bg-danger";
                    case COMPLETED -> "bg-success";
                    case NOT_STARTED -> "bg-secondary";
                };
            } catch (IllegalArgumentException e) {
                tasks = List.of();
                heading = "Unknown Status (" + status + ")";
                badgeClass = "bg-secondary";
            }
        }

        model.addAttribute("statusKey", status);
        model.addAttribute("heading", heading);
        model.addAttribute("badgeClass", badgeClass);
        model.addAttribute("count", tasks.size());
        model.addAttribute("tasks", tasks);
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("activeTab", "status");

        return "status/drilldown";
    }
}
