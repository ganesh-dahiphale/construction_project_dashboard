package com.example.dashboard.model;

public enum JobStatus {
    ACTIVE("Active"),
    ON_LEAVE("On Leave"),
    INACTIVE("Inactive");

    private final String displayName;

    JobStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
