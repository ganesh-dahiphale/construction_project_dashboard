package com.example.dashboard.security;

import com.example.dashboard.model.JobStatus;
import com.example.dashboard.model.Role;
import com.example.dashboard.model.User;
import com.example.dashboard.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
class AdminUserSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("Anonymous users redirected to /login on GET /admin/users")
    void anonymousRedirectedToLoginOnGet() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("Anonymous users redirected to /login on POST /admin/users")
    void anonymousRedirectedToLoginOnPost() throws Exception {
        mockMvc.perform(post("/admin/users").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "engineer", roles = {"ENGINEER"})
    @DisplayName("ENGINEER gets 403 on GET /admin/users")
    void engineerForbiddenOnGet() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "engineer", roles = {"ENGINEER"})
    @DisplayName("ENGINEER gets 403 on POST /admin/users")
    void engineerForbiddenOnPost() throws Exception {
        mockMvc.perform(post("/admin/users").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "engineer", roles = {"ENGINEER"})
    @DisplayName("ENGINEER gets 403 on POST /admin/users/{id}/status")
    void engineerForbiddenOnStatusChange() throws Exception {
        mockMvc.perform(post("/admin/users/1/status").with(csrf()).param("status", "INACTIVE"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "engineer", roles = {"ENGINEER"})
    @DisplayName("ENGINEER gets 403 on POST /admin/users/{id}/password")
    void engineerForbiddenOnPasswordReset() throws Exception {
        mockMvc.perform(post("/admin/users/1/password").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "engineer", roles = {"ENGINEER"})
    @DisplayName("ENGINEER gets 403 on POST /admin/users/{id}/delete")
    void engineerForbiddenOnDelete() throws Exception {
        mockMvc.perform(post("/admin/users/1/delete").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    @DisplayName("MANAGER gets 403 on GET /admin/users")
    void managerForbiddenOnGet() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    @DisplayName("MANAGER gets 403 on POST /admin/users")
    void managerForbiddenOnPost() throws Exception {
        mockMvc.perform(post("/admin/users").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("POST /admin/users without CSRF token is rejected with 403")
    void postWithoutCsrfForbidden() throws Exception {
        mockMvc.perform(post("/admin/users")
                        .param("username", "test.user")
                        .param("role", "ENGINEER"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Disabled or INACTIVE user cannot log in and gets inactive error message")
    void disabledUserCannotLogin() throws Exception {
        User inactiveUser = new User(
                "inactive.eng",
                passwordEncoder.encode("SecretPass123"),
                Role.ENGINEER,
                "Inactive Engineer",
                "inactive@example.com",
                JobStatus.INACTIVE,
                "admin"
        );
        inactiveUser.setEnabled(false);
        userRepository.save(inactiveUser);

        mockMvc.perform(formLogin().user("inactive.eng").password("SecretPass123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=inactive"));
    }

    @Test
    @DisplayName("ON_LEAVE user cannot log in and gets inactive error message")
    void onLeaveUserCannotLogin() throws Exception {
        User onLeaveUser = new User(
                "leave.eng",
                passwordEncoder.encode("SecretPass123"),
                Role.ENGINEER,
                "OnLeave Engineer",
                "onleave@example.com",
                JobStatus.ON_LEAVE,
                "admin"
        );
        userRepository.save(onLeaveUser);

        mockMvc.perform(formLogin().user("leave.eng").password("SecretPass123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=inactive"));
    }
}
