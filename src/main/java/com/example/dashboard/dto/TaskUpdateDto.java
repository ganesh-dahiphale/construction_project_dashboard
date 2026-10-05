package com.example.dashboard.dto;

import com.example.dashboard.model.TaskStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class TaskUpdateDto {

    @NotNull(message = "Status is required")
    private TaskStatus status;

    @NotNull(message = "Percent complete is required")
    @Min(value = 0, message = "Percent complete must be between 0 and 100")
    @Max(value = 100, message = "Percent complete must be between 0 and 100")
    private Integer percentComplete;

    @Size(max = 500, message = "Remarks cannot exceed 500 characters")
    private String remarks;

    public TaskUpdateDto() {
    }

    public TaskUpdateDto(TaskStatus status, Integer percentComplete, String remarks) {
        this.status = status;
        this.percentComplete = percentComplete;
        this.remarks = remarks;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public Integer getPercentComplete() {
        return percentComplete;
    }

    public void setPercentComplete(Integer percentComplete) {
        this.percentComplete = percentComplete;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
