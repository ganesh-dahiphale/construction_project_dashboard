package com.example.dashboard.controller;

import com.example.dashboard.dto.TaskAlertDto;
import com.example.dashboard.model.Task;
import com.example.dashboard.service.AlertService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/alerts")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping
    public String showAlertsPage(Model model) {
        List<TaskAlertDto> overdueTasks = alertService.getOverdueTasks();
        List<Task> blockedTasks = alertService.getBlockedTasks();
        int totalAlerts = overdueTasks.size() + blockedTasks.size();

        model.addAttribute("overdueTasks", overdueTasks);
        model.addAttribute("blockedTasks", blockedTasks);
        model.addAttribute("totalAlerts", totalAlerts);
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("activeTab", "alerts");

        return "alerts";
    }
}
