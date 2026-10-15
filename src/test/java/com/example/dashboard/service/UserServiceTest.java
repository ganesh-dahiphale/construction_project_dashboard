package com.example.dashboard.service;

import com.example.dashboard.dto.UserRegistrationDto;
import com.example.dashboard.model.Role;
import com.example.dashboard.model.User;
import com.example.dashboard.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private UserRegistrationDto validDto;

    @BeforeEach
    void setUp() {
        validDto = new UserRegistrationDto("newengineer", "securePass123", "securePass123");
    }

    @Test
    @DisplayName("Register user successfully allocates Role.ENGINEER by default")
    void registerUserAllocatesEngineerRoleByDefault() {
        when(userRepository.existsByUsername("newengineer")).thenReturn(false);
        when(passwordEncoder.encode("securePass123")).thenReturn("encodedPasswordHash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User registered = userService.registerUser(validDto);

        assertThat(registered.getUsername()).isEqualTo("newengineer");
        assertThat(registered.getPassword()).isEqualTo("encodedPasswordHash");
        assertThat(registered.getRole()).isEqualTo(Role.ENGINEER);
        assertThat(registered.isEnabled()).isTrue();

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getRole()).isEqualTo(Role.ENGINEER);
    }

    @Test
    @DisplayName("Register user throws exception if passwords do not match")
    void registerUserThrowsExceptionOnPasswordMismatch() {
        validDto.setConfirmPassword("differentPassword");

        assertThatThrownBy(() -> userService.registerUser(validDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Passwords do not match");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Register user throws exception if username already exists")
    void registerUserThrowsExceptionOnDuplicateUsername() {
        when(userRepository.existsByUsername("newengineer")).thenReturn(true);

        assertThatThrownBy(() -> userService.registerUser(validDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already registered");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Admin can update user role from ENGINEER to MANAGER")
    void adminCanUpdateUserRoleFromEngineerToManager() {
        User existingUser = new User("field.eng", "hash", Role.ENGINEER);
        when(userRepository.findById(10L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updated = userService.updateUserRole(10L, Role.MANAGER, "admin");

        assertThat(updated.getRole()).isEqualTo(Role.MANAGER);
        verify(userRepository).save(existingUser);
    }

    @Test
    @DisplayName("Admin cannot demote their own active account")
    void adminCannotDemoteOwnAccount() {
        User adminUser = new User("admin", "hash", Role.ADMIN);
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));

        assertThatThrownBy(() -> userService.updateUserRole(1L, Role.ENGINEER, "admin"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot demote your own active administrator account");

        verify(userRepository, never()).save(any());
    }
}
