package com.example.dashboard.controller;

import com.example.dashboard.dto.DashboardFilter;
import com.example.dashboard.dto.DashboardSummary;
import com.example.dashboard.model.Task;
import com.example.dashboard.model.TaskStatus;
import com.example.dashboard.service.DashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public String showDashboard(@ModelAttribute("filter") DashboardFilter filter, Model model) {
        DashboardSummary summary = dashboardService.computeSummary();
        List<Task> tasks = dashboardService.search(filter);

        model.addAttribute("summary", summary);
        model.addAttribute("tasks", tasks);
        model.addAttribute("projects", dashboardService.getAllProjects());
        model.addAttribute("statuses", TaskStatus.values());
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("activeTab", "dashboard");

        return "dashboard";
    }
}
