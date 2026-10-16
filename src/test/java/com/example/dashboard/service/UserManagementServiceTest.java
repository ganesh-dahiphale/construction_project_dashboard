package com.example.dashboard.service;

import com.example.dashboard.dto.PasswordResetRequest;
import com.example.dashboard.dto.UserRegistrationRequest;
import com.example.dashboard.dto.UserUpdateRequest;
import com.example.dashboard.model.JobStatus;
import com.example.dashboard.model.Project;
import com.example.dashboard.model.Role;
import com.example.dashboard.model.Task;
import com.example.dashboard.model.TaskStatus;
import com.example.dashboard.model.User;
import com.example.dashboard.repository.TaskRepository;
import com.example.dashboard.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserManagementServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SessionRegistry sessionRegistry;

    @Mock
    private TaskRepository taskRepository;

    private UserManagementService service;

    @BeforeEach
    void setUp() {
        service = new UserManagementService(userRepository, passwordEncoder, sessionRegistry);
    }

    @Test
    @DisplayName("registerUser: successfully registers engineer with hashed password")
    void registerUserSuccess() {
        UserRegistrationRequest req = new UserRegistrationRequest(
                "Jane Doe", "jane@example.com", "jane.doe", Role.ENGINEER, "Password123", "Password123"
        );

        when(userRepository.existsByEmail("jane@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("jane.doe")).thenReturn(false);
        when(passwordEncoder.encode("Password123")).thenReturn("$2a$10$hashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User created = service.registerUser(req, "admin");

        assertThat(created.getUsername()).isEqualTo("jane.doe");
        assertThat(created.getEmail()).isEqualTo("jane@example.com");
        assertThat(created.getFullName()).isEqualTo("Jane Doe");
        assertThat(created.getRole()).isEqualTo(Role.ENGINEER);
        assertThat(created.getJobStatus()).isEqualTo(JobStatus.ACTIVE);
        assertThat(created.getPassword()).isEqualTo("$2a$10$hashedPassword");
        assertThat(created.getPassword()).doesNotContain("Password123");
        verify(passwordEncoder).encode("Password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("registerUser: rejects ADMIN role")
    void registerUserRejectsAdminRole() {
        UserRegistrationRequest req = new UserRegistrationRequest(
                "Fake Admin", "fakeadmin@example.com", "fake.admin", Role.ADMIN, "Password123", "Password123"
        );

        assertThatThrownBy(() -> service.registerUser(req, "admin"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Administrator accounts cannot be created");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("registerUser: rejects duplicate username")
    void registerUserRejectsDuplicateUsername() {
        UserRegistrationRequest req = new UserRegistrationRequest(
                "Jane Doe", "jane@example.com", "existing.user", Role.ENGINEER, "Password123", "Password123"
        );
        when(userRepository.existsByEmail("jane@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("existing.user")).thenReturn(true);

        assertThatThrownBy(() -> service.registerUser(req, "admin"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already registered");
    }

    @Test
    @DisplayName("registerUser: rejects duplicate email")
    void registerUserRejectsDuplicateEmail() {
        UserRegistrationRequest req = new UserRegistrationRequest(
                "Jane Doe", "existing@example.com", "new.user", Role.ENGINEER, "Password123", "Password123"
        );
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.registerUser(req, "admin"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already registered");
    }

    @Test
    @DisplayName("registerUser: rejects weak password (less than 8 chars or missing letter/digit)")
    void registerUserRejectsWeakPassword() {
        UserRegistrationRequest shortPass = new UserRegistrationRequest(
                "Jane Doe", "jane@example.com", "jane.doe", Role.ENGINEER, "Pass1", "Pass1"
        );
        assertThatThrownBy(() -> service.registerUser(shortPass, "admin"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least 8 characters");

        UserRegistrationRequest noDigit = new UserRegistrationRequest(
                "Jane Doe", "jane@example.com", "jane.doe", Role.ENGINEER, "PasswordOnly", "PasswordOnly"
        );
        assertThatThrownBy(() -> service.registerUser(noDigit, "admin"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least one letter and at least one digit");
    }

    @Test
    @DisplayName("registerUser: rejects mismatched confirm password")
    void registerUserRejectsMismatchedPasswords() {
        UserRegistrationRequest mismatch = new UserRegistrationRequest(
                "Jane Doe", "jane@example.com", "jane.doe", Role.ENGINEER, "Password123", "DiffPassword456"
        );
        assertThatThrownBy(() -> service.registerUser(mismatch, "admin"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Passwords do not match");
    }

    @Test
    @DisplayName("updateUser: admin cannot change role of their own account")
    void adminCannotChangeOwnRole() {
        User admin = new User("admin", "hash", Role.ADMIN);
        admin.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));

        UserUpdateRequest req = new UserUpdateRequest("Admin User", "admin@example.com", Role.ENGINEER);

        assertThatThrownBy(() -> service.updateUser(1L, req, "admin"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Administrator accounts cannot be modified");
    }

    @Test
    @DisplayName("updateUser: cannot edit an ADMIN account")
    void cannotEditAdminAccount() {
        User admin2 = new User("otheradmin", "hash", Role.ADMIN);
        admin2.setId(2L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(admin2));

        UserUpdateRequest req = new UserUpdateRequest("Other Admin", "other@example.com", Role.MANAGER);

        assertThatThrownBy(() -> service.updateUser(2L, req, "admin"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Administrator accounts cannot be modified");
    }

    @Test
    @DisplayName("changeStatus: admin cannot deactivate their own account")
    void adminCannotDeactivateSelf() {
        User admin = new User("admin", "hash", Role.ADMIN);
        admin.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> service.changeStatus(1L, JobStatus.INACTIVE, "admin"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("changeStatus: successfully updates engineer status to INACTIVE and invalidates session")
    void changeStatusSuccess() {
        User engineer = new User("eng.bob", "hash", Role.ENGINEER);
        engineer.setId(10L);
        when(userRepository.findById(10L)).thenReturn(Optional.of(engineer));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User updated = service.changeStatus(10L, JobStatus.INACTIVE, "admin");

        assertThat(updated.getJobStatus()).isEqualTo(JobStatus.INACTIVE);
        assertThat(updated.isEnabled()).isFalse();
        verify(userRepository).save(engineer);
    }

    @Test
    @DisplayName("deleteUser: admin cannot delete their own account")
    void adminCannotDeleteSelf() {
        User admin = new User("admin", "hash", Role.ADMIN);
        admin.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> service.deleteUser(1L, "admin"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("You cannot delete your own administrator account");
    }

    @Test
    @DisplayName("deleteUser: cannot delete ADMIN accounts through user management")
    void cannotDeleteAdminAccount() {
        User admin2 = new User("admin2", "hash", Role.ADMIN);
        admin2.setId(2L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(admin2));

        assertThatThrownBy(() -> service.deleteUser(2L, "admin"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Administrator accounts cannot be deleted");
    }

    @Test
    @DisplayName("deleteUser: successfully deletes engineer and preserves task updatedBy")
    void deleteUserPreservesTasks() {
        User engineer = new User("eng.mark", "hash", Role.ENGINEER);
        engineer.setId(15L);
        when(userRepository.findById(15L)).thenReturn(Optional.of(engineer));

        // Create a task that has updatedBy = "eng.mark"
        Project project = new Project("Test Proj", "Location", LocalDate.now(), LocalDate.now().plusMonths(6));
        Task task = new Task(project, "Inspect site", TaskStatus.IN_PROGRESS, 50, LocalDate.now().plusDays(5), "Notes");
        task.setUpdatedBy("eng.mark");

        service.deleteUser(15L, "admin");

        verify(userRepository).delete(engineer);
        // Task remains untouched with updatedBy unchanged
        assertThat(task.getUpdatedBy()).isEqualTo("eng.mark");
    }

    @Test
    @DisplayName("resetPassword: sets new hashed password and never stores plaintext")
    void resetPasswordSuccess() {
        User engineer = new User("eng.alex", "oldHash", Role.ENGINEER);
        engineer.setId(20L);
        when(userRepository.findById(20L)).thenReturn(Optional.of(engineer));
        when(passwordEncoder.encode("NewSecret890")).thenReturn("$2a$10$newHashedSecret");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        PasswordResetRequest req = new PasswordResetRequest("NewSecret890", "NewSecret890");
        User updated = service.resetPassword(20L, req, "admin");

        assertThat(updated.getPassword()).isEqualTo("$2a$10$newHashedSecret");
        assertThat(updated.getPassword()).doesNotContain("NewSecret890");
        verify(passwordEncoder).encode("NewSecret890");
        verify(userRepository).save(engineer);
    }
}
