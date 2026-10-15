package com.example.dashboard.controller;

import com.example.dashboard.model.Role;
import com.example.dashboard.model.User;
import com.example.dashboard.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String listUsers(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        model.addAttribute("roles", Role.values());
        model.addAttribute("activeTab", "admin");
        return "admin/users/list";
    }

    @PostMapping("/{id}/role")
    public String updateUserRole(
            @PathVariable("id") Long id,
            @RequestParam("role") Role role,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        String currentAdmin = authentication != null ? authentication.getName() : null;

        try {
            User updatedUser = userService.updateUserRole(id, role, currentAdmin);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Role for user '" + updatedUser.getUsername() + "' updated to " + role.getDisplayName() + " (" + role.name() + ") successfully!");
        } catch (IllegalStateException | IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/admin/users";
    }
}
