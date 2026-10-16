package com.example.dashboard.service;

import com.example.dashboard.dto.PasswordResetRequest;
import com.example.dashboard.dto.UserRegistrationRequest;
import com.example.dashboard.dto.UserUpdateRequest;
import com.example.dashboard.model.JobStatus;
import com.example.dashboard.model.Role;
import com.example.dashboard.model.User;
import com.example.dashboard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

@Service
@Transactional
@PreAuthorize("hasRole('ADMIN')")
public class UserManagementService {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]+$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-zA-Z])(?=.*\\d).{8,}$");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SessionRegistry sessionRegistry;

    public UserManagementService(UserRepository userRepository,
                                 PasswordEncoder passwordEncoder,
                                 @Autowired(required = false) SessionRegistry sessionRegistry) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.sessionRegistry = sessionRegistry;
    }

    @Transactional(readOnly = true)
    public List<User> listUsers(String search, Role role, JobStatus status) {
        String query = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        return userRepository.searchUsers(query, role, status);
    }

    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));
    }

    public User registerUser(UserRegistrationRequest request, String currentAdminUsername) {
        if (request == null) {
            throw new IllegalArgumentException("Registration request cannot be null");
        }

        String fullName = request.getFullName() != null ? request.getFullName().trim() : "";
        if (fullName.isEmpty() || fullName.length() > 100) {
            throw new IllegalArgumentException("Full name is required and must not exceed 100 characters.");
        }

        String email = request.getEmail() != null ? request.getEmail().trim().toLowerCase() : "";
        if (email.isEmpty() || !EMAIL_PATTERN.matcher(email).matches() || email.length() > 100) {
            throw new IllegalArgumentException("A valid email address is required (up to 100 characters).");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email address '" + email + "' is already registered.");
        }

        String username = request.getUsername() != null ? request.getUsername().trim() : "";
        if (username.length() < 3 || username.length() > 30 || !USERNAME_PATTERN.matcher(username).matches()) {
            throw new IllegalArgumentException("Username must be between 3 and 30 characters and contain only letters, digits, dots, underscores, or hyphens.");
        }
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username '" + username + "' is already registered.");
        }

        if (request.getRole() == null || request.getRole() == Role.ADMIN) {
            throw new IllegalArgumentException("Administrator accounts cannot be created through this feature. Role must be ENGINEER or MANAGER.");
        }

        validatePassword(request.getPassword(), request.getConfirmPassword());

        User user = new User(
                username,
                passwordEncoder.encode(request.getPassword()),
                request.getRole(),
                fullName,
                email,
                JobStatus.ACTIVE,
                (currentAdminUsername != null && !currentAdminUsername.isBlank()) ? currentAdminUsername : "admin"
        );
        user.setEnabled(true);

        return userRepository.save(user);
    }

    public User updateUser(Long id, UserUpdateRequest request, String currentAdminUsername) {
        User user = getUserById(id);

        if (user.getRole() == Role.ADMIN) {
            throw new IllegalStateException("Administrator accounts cannot be modified through the user management interface.");
        }

        if (currentAdminUsername != null && user.getUsername().equalsIgnoreCase(currentAdminUsername) && request.getRole() != user.getRole()) {
            throw new IllegalStateException("You cannot change the role of your own administrator account.");
        }

        if (request.getRole() == null || request.getRole() == Role.ADMIN) {
            throw new IllegalArgumentException("User role must be either ENGINEER or MANAGER.");
        }

        String fullName = request.getFullName() != null ? request.getFullName().trim() : "";
        if (fullName.isEmpty() || fullName.length() > 100) {
            throw new IllegalArgumentException("Full name is required and must not exceed 100 characters.");
        }

        String email = request.getEmail() != null ? request.getEmail().trim().toLowerCase() : "";
        if (email.isEmpty() || !EMAIL_PATTERN.matcher(email).matches() || email.length() > 100) {
            throw new IllegalArgumentException("A valid email address is required.");
        }

        userRepository.findByEmail(email).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new IllegalArgumentException("Email address '" + email + "' is already in use by another user.");
            }
        });

        user.setFullName(fullName);
        user.setEmail(email);
        user.setRole(request.getRole());
        user.setLastUpdated(LocalDateTime.now());

        return userRepository.save(user);
    }

    public User changeStatus(Long id, JobStatus newStatus, String currentAdminUsername) {
        if (newStatus == null) {
            throw new IllegalArgumentException("Job status cannot be null");
        }

        User user = getUserById(id);

        if (user.getRole() == Role.ADMIN) {
            throw new IllegalStateException("Administrator accounts cannot be modified through the user management interface.");
        }

        if (currentAdminUsername != null && user.getUsername().equalsIgnoreCase(currentAdminUsername)) {
            throw new IllegalStateException("You cannot change the status of your own administrator account.");
        }

        if (user.getRole() == Role.ADMIN && user.getJobStatus() == JobStatus.ACTIVE && newStatus != JobStatus.ACTIVE) {
            long activeAdmins = userRepository.countByRoleAndJobStatus(Role.ADMIN, JobStatus.ACTIVE);
            if (activeAdmins <= 1) {
                throw new IllegalStateException("The last active administrator account cannot be deactivated.");
            }
        }

        user.setJobStatus(newStatus);
        user.setEnabled(newStatus == JobStatus.ACTIVE);
        user.setLastUpdated(LocalDateTime.now());

        User saved = userRepository.save(user);

        if (newStatus != JobStatus.ACTIVE) {
            invalidateUserSessions(user.getUsername());
        }

        return saved;
    }

    public User resetPassword(Long id, PasswordResetRequest request, String currentAdminUsername) {
        User user = getUserById(id);

        if (user.getRole() == Role.ADMIN) {
            throw new IllegalStateException("Administrator accounts cannot be modified through the user management interface.");
        }

        validatePassword(request.getNewPassword(), request.getConfirmPassword());

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setLastUpdated(LocalDateTime.now());

        User saved = userRepository.save(user);
        invalidateUserSessions(user.getUsername());
        return saved;
    }

    public void deleteUser(Long id, String currentAdminUsername) {
        User user = getUserById(id);

        if (currentAdminUsername != null && user.getUsername().equalsIgnoreCase(currentAdminUsername)) {
            throw new IllegalStateException("You cannot delete your own administrator account.");
        }

        if (user.getRole() == Role.ADMIN) {
            throw new IllegalStateException("Administrator accounts cannot be deleted through the user management interface.");
        }

        long activeAdmins = userRepository.countByRoleAndJobStatus(Role.ADMIN, JobStatus.ACTIVE);
        if (user.getRole() == Role.ADMIN && activeAdmins <= 1) {
            throw new IllegalStateException("The last active administrator account cannot be deleted.");
        }

        invalidateUserSessions(user.getUsername());
        userRepository.delete(user);
    }

    public void invalidateUserSessions(String username) {
        if (sessionRegistry == null || username == null || username.isBlank()) {
            return;
        }

        for (Object principal : sessionRegistry.getAllPrincipals()) {
            String principalUsername = null;
            if (principal instanceof UserDetails ud) {
                principalUsername = ud.getUsername();
            } else if (principal instanceof Principal p) {
                principalUsername = p.getName();
            } else if (principal instanceof String s) {
                principalUsername = s;
            }

            if (username.equalsIgnoreCase(principalUsername)) {
                List<SessionInformation> sessions = sessionRegistry.getAllSessions(principal, false);
                for (SessionInformation session : sessions) {
                    session.expireNow();
                }
            }
        }
    }

    private void validatePassword(String password, String confirmPassword) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long.");
        }
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new IllegalArgumentException("Password must contain at least one letter and at least one digit.");
        }
        if (confirmPassword == null || !password.equals(confirmPassword)) {
            throw new IllegalArgumentException("Passwords do not match.");
        }
    }
}
