package com.example.dashboard.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class DashboardSummary {
    private final long totalTasks;
    private final long completedTasks;
    private final long inProgressTasks;
    private final long blockedTasks;
    private final long delayedTasks;
    private final double overallCompletionPercentage;

    public DashboardSummary(long totalTasks, long completedTasks, long inProgressTasks,
                            long blockedTasks, long delayedTasks, double overallCompletionPercentage) {
        this.totalTasks = totalTasks;
        this.completedTasks = completedTasks;
        this.inProgressTasks = inProgressTasks;
        this.blockedTasks = blockedTasks;
        this.delayedTasks = delayedTasks;
        this.overallCompletionPercentage = BigDecimal.valueOf(overallCompletionPercentage)
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();
    }

    public long getTotalTasks() {
        return totalTasks;
    }

    public long getCompletedTasks() {
        return completedTasks;
    }

    public long getInProgressTasks() {
        return inProgressTasks;
    }

    public long getBlockedTasks() {
        return blockedTasks;
    }

    public long getDelayedTasks() {
        return delayedTasks;
    }

    public double getOverallCompletionPercentage() {
        return overallCompletionPercentage;
    }
}
