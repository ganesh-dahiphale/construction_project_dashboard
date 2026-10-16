package com.example.dashboard.controller;

import com.example.dashboard.dto.PasswordResetRequest;
import com.example.dashboard.dto.UserRegistrationRequest;
import com.example.dashboard.dto.UserUpdateRequest;
import com.example.dashboard.model.JobStatus;
import com.example.dashboard.model.Role;
import com.example.dashboard.model.User;
import com.example.dashboard.service.UserManagementService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserManagementService userManagementService;

    public AdminUserController(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    @GetMapping
    public String listUsers(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "role", required = false) Role role,
            @RequestParam(value = "status", required = false) JobStatus status,
            Model model) {

        List<User> users = userManagementService.listUsers(search, role, status);
        model.addAttribute("users", users);
        model.addAttribute("roles", Role.values());
        model.addAttribute("assignableRoles", new Role[]{Role.ENGINEER, Role.MANAGER});
        model.addAttribute("statuses", JobStatus.values());
        model.addAttribute("search", search);
        model.addAttribute("selectedRole", role);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("activeTab", "admin");

        return "admin/users/list";
    }

    @GetMapping("/new")
    public String showCreateUserForm(Model model) {
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new UserRegistrationRequest());
        }
        model.addAttribute("roles", new Role[]{Role.ENGINEER, Role.MANAGER});
        model.addAttribute("activeTab", "admin");
        return "admin/users/new";
    }

    @PostMapping
    public String createUser(
            @Valid @ModelAttribute("user") UserRegistrationRequest request,
            BindingResult bindingResult,
            Authentication authentication,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("roles", new Role[]{Role.ENGINEER, Role.MANAGER});
            model.addAttribute("activeTab", "admin");
            return "admin/users/new";
        }

        try {
            String adminUsername = authentication != null ? authentication.getName() : "admin";
            User created = userManagementService.registerUser(request, adminUsername);
            redirectAttributes.addFlashAttribute("successMessage",
                    "User '" + created.getUsername() + "' registered successfully with role " + created.getRole().getDisplayName() + "!");
            return "redirect:/admin/users";
        } catch (IllegalArgumentException | IllegalStateException ex) {
            model.addAttribute("roles", new Role[]{Role.ENGINEER, Role.MANAGER});
            model.addAttribute("activeTab", "admin");
            model.addAttribute("errorMessage", ex.getMessage());
            return "admin/users/new";
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditUserForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        User user;
        try {
            user = userManagementService.getUserById(id);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with ID: " + id);
        }

        if (user.getRole() == Role.ADMIN) {
            redirectAttributes.addFlashAttribute("errorMessage", "Administrator accounts cannot be edited.");
            return "redirect:/admin/users";
        }

        if (!model.containsAttribute("user")) {
            UserUpdateRequest request = new UserUpdateRequest(user.getFullName(), user.getEmail(), user.getRole());
            model.addAttribute("user", request);
        }
        model.addAttribute("targetUser", user);
        model.addAttribute("roles", new Role[]{Role.ENGINEER, Role.MANAGER});
        model.addAttribute("activeTab", "admin");
        return "admin/users/edit";
    }

    @PostMapping("/{id}")
    public String updateUser(
            @PathVariable("id") Long id,
            @Valid @ModelAttribute("user") UserUpdateRequest request,
            BindingResult bindingResult,
            Authentication authentication,
            RedirectAttributes redirectAttributes,
            Model model) {

        User targetUser;
        try {
            targetUser = userManagementService.getUserById(id);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with ID: " + id);
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("targetUser", targetUser);
            model.addAttribute("roles", new Role[]{Role.ENGINEER, Role.MANAGER});
            model.addAttribute("activeTab", "admin");
            return "admin/users/edit";
        }

        try {
            String adminUsername = authentication != null ? authentication.getName() : "admin";
            User updated = userManagementService.updateUser(id, request, adminUsername);
            redirectAttributes.addFlashAttribute("successMessage",
                    "User '" + updated.getUsername() + "' updated successfully!");
            return "redirect:/admin/users";
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/admin/users";
        }
    }

    @PostMapping("/{id}/status")
    public String changeUserStatus(
            @PathVariable("id") Long id,
            @RequestParam("status") JobStatus status,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            String adminUsername = authentication != null ? authentication.getName() : "admin";
            User updated = userManagementService.changeStatus(id, status, adminUsername);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Job status for user '" + updated.getUsername() + "' changed to " + updated.getJobStatus().getDisplayName() + ".");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/password")
    public String resetUserPassword(
            @PathVariable("id") Long id,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            String adminUsername = authentication != null ? authentication.getName() : "admin";
            PasswordResetRequest request = new PasswordResetRequest(newPassword, confirmPassword);
            User updated = userManagementService.resetPassword(id, request, adminUsername);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Password for user '" + updated.getUsername() + "' reset successfully.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/delete")
    public String deleteUser(
            @PathVariable("id") Long id,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            String adminUsername = authentication != null ? authentication.getName() : "admin";
            userManagementService.deleteUser(id, adminUsername);
            redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/role")
    public String updateUserRoleLegacy(
            @PathVariable("id") Long id,
            @RequestParam("role") Role role,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            String adminUsername = authentication != null ? authentication.getName() : "admin";
            User user = userManagementService.getUserById(id);
            UserUpdateRequest request = new UserUpdateRequest(user.getFullName(), user.getEmail(), role);
            User updated = userManagementService.updateUser(id, request, adminUsername);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Role for user '" + updated.getUsername() + "' updated to " + role.getDisplayName() + " (" + role.name() + ") successfully!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/admin/users";
    }
}
