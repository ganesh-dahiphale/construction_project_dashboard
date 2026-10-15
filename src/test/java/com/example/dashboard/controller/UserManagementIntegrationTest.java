package com.example.dashboard.controller;

import com.example.dashboard.model.Role;
import com.example.dashboard.model.User;
import com.example.dashboard.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
class UserManagementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Public can access GET /register")
    void publicCanAccessRegisterPage() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    @DisplayName("POST /register creates user with default role ENGINEER and redirects to /login")
    void postRegisterCreatesUserWithDefaultEngineerRole() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("username", "site.lead.rahul")
                        .param("password", "strongPassword123")
                        .param("confirmPassword", "strongPassword123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attributeExists("registrationSuccess"));

        User savedUser = userRepository.findByUsername("site.lead.rahul")
                .orElseThrow(() -> new AssertionError("User should exist in database"));

        assertThat(savedUser.getRole()).isEqualTo(Role.ENGINEER);
        assertThat(savedUser.isEnabled()).isTrue();
    }

    @Test
    @DisplayName("POST /register with mismatched passwords rejects with validation error")
    void postRegisterWithMismatchedPasswordsFails() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("username", "new.user")
                        .param("password", "password123")
                        .param("confirmPassword", "differentPassword123"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().hasErrors());

        assertThat(userRepository.findByUsername("new.user")).isEmpty();
    }

    @Test
    @DisplayName("Anonymous users cannot access GET /admin/users")
    void anonymousCannotAccessAdminUsers() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "engineer", roles = {"ENGINEER"})
    @DisplayName("Engineers cannot access GET /admin/users (403 Forbidden)")
    void engineerCannotAccessAdminUsers() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    @DisplayName("Managers cannot access GET /admin/users (403 Forbidden)")
    void managerCannotAccessAdminUsers() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Admins can access GET /admin/users and view registered users")
    void adminCanAccessAdminUsers() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/users/list"))
                .andExpect(model().attributeExists("users"))
                .andExpect(model().attributeExists("roles"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Admins can change user role from ENGINEER to MANAGER via POST /admin/users/{id}/role")
    void adminCanChangeUserRole() throws Exception {
        User testEngineer = userRepository.findByUsername("engineer")
                .orElseThrow(() -> new AssertionError("Seed user 'engineer' should exist"));

        assertThat(testEngineer.getRole()).isEqualTo(Role.ENGINEER);

        mockMvc.perform(post("/admin/users/" + testEngineer.getId() + "/role")
                        .with(csrf())
                        .param("role", "MANAGER"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"))
                .andExpect(flash().attributeExists("successMessage"));

        User updatedUser = userRepository.findById(testEngineer.getId())
                .orElseThrow(() -> new AssertionError("User should exist"));

        assertThat(updatedUser.getRole()).isEqualTo(Role.MANAGER);
    }
}
