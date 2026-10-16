package com.example.dashboard.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Default constructor and builder sets ACTIVE job status")
    void defaultJobStatusIsActive() {
        User user = new User("eng.jane", "hashedPass123", Role.ENGINEER);
        assertThat(user.getJobStatus()).isEqualTo(JobStatus.ACTIVE);
        assertThat(user.getFullName()).isEqualTo("Eng.jane");
        assertThat(user.getEmail()).isEqualTo("eng.jane@example.com");
        assertThat(user.isEnabled()).isTrue();
        assertThat(user.getCreatedBy()).isEqualTo("system");
    }

    @Test
    @DisplayName("Full constructor sets all user profile fields correctly")
    void fullConstructorSetsAllFields() {
        User user = new User("lead.mgr", "hashedPass123", Role.MANAGER, "Lead Manager", "mgr@construction.com", JobStatus.ON_LEAVE, "admin");
        assertThat(user.getUsername()).isEqualTo("lead.mgr");
        assertThat(user.getRole()).isEqualTo(Role.MANAGER);
        assertThat(user.getFullName()).isEqualTo("Lead Manager");
        assertThat(user.getEmail()).isEqualTo("mgr@construction.com");
        assertThat(user.getJobStatus()).isEqualTo(JobStatus.ON_LEAVE);
        assertThat(user.getCreatedBy()).isEqualTo("admin");
    }

    @Test
    @DisplayName("Valid user passes validation")
    void validUserPassesValidation() {
        User user = new User("valid_user.1", "hashedPass123", Role.ENGINEER, "Valid User", "valid@example.com", JobStatus.ACTIVE, "admin");
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Invalid username rejected: too short, invalid characters")
    void invalidUsernameRejected() {
        User shortUsername = new User("ab", "hashedPass123", Role.ENGINEER, "Short Name", "short@example.com", JobStatus.ACTIVE, "admin");
        Set<ConstraintViolation<User>> violations = validator.validate(shortUsername);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("username"));

        User badChars = new User("user@invalid!", "hashedPass123", Role.ENGINEER, "Bad Chars", "bad@example.com", JobStatus.ACTIVE, "admin");
        violations = validator.validate(badChars);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("username"));
    }

    @Test
    @DisplayName("Invalid email rejected")
    void invalidEmailRejected() {
        User invalidEmail = new User("user.valid", "hashedPass123", Role.ENGINEER, "Valid Name", "not-an-email", JobStatus.ACTIVE, "admin");
        Set<ConstraintViolation<User>> violations = validator.validate(invalidEmail);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }

    @Test
    @DisplayName("PrePersist hook sets createdAt, lastUpdated, and defaults if null")
    void prePersistSetsDefaults() {
        User user = new User();
        user.setUsername("raw.user");
        user.setPassword("hashedPass123");
        user.setRole(Role.ENGINEER);
        user.onCreate();

        assertThat(user.getCreatedAt()).isNotNull();
        assertThat(user.getLastUpdated()).isNotNull();
        assertThat(user.getJobStatus()).isEqualTo(JobStatus.ACTIVE);
        assertThat(user.getFullName()).isEqualTo("raw.user");
        assertThat(user.getEmail()).isEqualTo("raw.user@example.com");
    }
}
