package com.example.dashboard.dto;

import com.example.dashboard.model.Task;

public class TaskAlertDto {
    private final Task task;
    private final long daysOverdue;
    private final String alertType; // "OVERDUE" or "BLOCKED"

    public TaskAlertDto(Task task, long daysOverdue, String alertType) {
        this.task = task;
        this.daysOverdue = daysOverdue;
        this.alertType = alertType;
    }

    public Task getTask() {
        return task;
    }

    public long getDaysOverdue() {
        return daysOverdue;
    }

    public String getAlertType() {
        return alertType;
    }
}
