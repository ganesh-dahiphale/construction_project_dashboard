package com.example.dashboard.service;

import com.example.dashboard.dto.UserRegistrationDto;
import com.example.dashboard.model.Role;
import com.example.dashboard.model.User;
import com.example.dashboard.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public User registerUser(UserRegistrationDto dto) {
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        String trimmedUsername = dto.getUsername().trim();
        if (userRepository.existsByUsername(trimmedUsername)) {
            throw new IllegalArgumentException("Username '" + trimmedUsername + "' is already registered");
        }

        // New users are allocated the role of ENGINEER by default
        User newUser = new User(
                trimmedUsername,
                passwordEncoder.encode(dto.getPassword()),
                Role.ENGINEER
        );
        newUser.setEnabled(true);

        return userRepository.save(newUser);
    }

    public User updateUserRole(Long userId, Role newRole, String currentAdminUsername) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        if (currentAdminUsername != null && user.getUsername().equalsIgnoreCase(currentAdminUsername) && newRole != Role.ADMIN) {
            throw new IllegalStateException("You cannot demote your own active administrator account.");
        }

        user.setRole(newRole);
        return userRepository.save(user);
    }
}
