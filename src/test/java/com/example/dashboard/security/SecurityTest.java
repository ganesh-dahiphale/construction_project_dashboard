package com.example.dashboard.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class SecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Public endpoints: /health and /login are accessible anonymously")
    void publicEndpointsAccessibleAnonymously() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Anonymous users accessing protected pages are redirected to /login")
    void anonymousUserRedirectedToLogin() throws Exception {
        mockMvc.perform(get("/tasks"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));

        mockMvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));

        mockMvc.perform(get("/status"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));

        mockMvc.perform(get("/alerts"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("Form login with valid seeded credentials authenticates successfully")
    void formLoginSuccess() throws Exception {
        mockMvc.perform(formLogin().user("engineer").password("engineer123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(authenticated().withUsername("engineer").withRoles("ENGINEER"));
    }

    @Test
    @DisplayName("Form login with invalid password fails and redirects to /login?error=true")
    void formLoginFailure() throws Exception {
        mockMvc.perform(formLogin().user("engineer").password("wrongpassword"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=true"))
                .andExpect(unauthenticated());
    }

    @Test
    @WithMockUser(username = "engineer", roles = {"ENGINEER"})
    @DisplayName("ROLE_ENGINEER can access /tasks but gets 403 on /dashboard, /alerts, and /admin/projects")
    void engineerRoleAccessRestrictions() throws Exception {
        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/status"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/alerts"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/admin/projects"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    @DisplayName("ROLE_MANAGER can access /tasks, /dashboard, /status, /alerts but gets 403 on /admin/projects")
    void managerRoleAccessAllowed() throws Exception {
        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/status"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/alerts"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/admin/projects"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("ROLE_ADMIN has full access to all application views including /admin/projects")
    void adminRoleHasFullAccess() throws Exception {
        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/alerts"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/admin/projects"))
                .andExpect(status().isOk());
    }
}
