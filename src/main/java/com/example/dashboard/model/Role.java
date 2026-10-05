package com.example.dashboard.model;

public enum Role {
    ADMIN("Administrator"),
    MANAGER("Project Manager"),
    ENGINEER("Site Engineer");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getAuthority() {
        return "ROLE_" + this.name();
    }
}
