package com.library.digital_library.service;

import com.library.digital_library.dto.AuthRequest;
import com.library.digital_library.model.User;
import com.library.digital_library.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Set;

@Service
public class AuthService {

    private static final Set<String> ALLOWED_ROLES = Set.of("ADMIN", "USER");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User registerPublicUser(AuthRequest request) {
        validateCredentials(request);
        String username = request.getUsername().trim();
        String email = request.getEmail().trim();
        checkDuplicate(username, email);

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER");

        return userRepository.save(user);
    }

    @Transactional
    public User createUserByAdmin(AuthRequest request) {
        validateCredentials(request);
        String username = request.getUsername().trim();
        String email = request.getEmail().trim();
        checkDuplicate(username, email);

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        String role = request.getRole() == null || request.getRole().isBlank()
                ? "USER"
                : request.getRole().trim().toUpperCase(Locale.ROOT);
        if (!ALLOWED_ROLES.contains(role)) {
            throw new IllegalArgumentException("Role must be ADMIN or USER");
        }
        user.setRole(role);

        return userRepository.save(user);
    }

    private void validateCredentials(AuthRequest request) {
        if (request == null
                || request.getUsername() == null || request.getUsername().isBlank()
                || request.getEmail() == null || request.getEmail().isBlank()
                || request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("username, email and password are required");
        }
    }

    private void checkDuplicate(String username, String email) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalStateException("Username already exists");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalStateException("Email already exists");
        }
    }
}