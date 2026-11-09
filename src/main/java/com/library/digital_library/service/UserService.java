package com.library.digital_library.service;

import com.library.digital_library.dto.UserUpdateRequest;
import com.library.digital_library.model.User;
import com.library.digital_library.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Set<String> ALLOWED_ROLES = Set.of("ADMIN", "USER");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản người dùng!"));
        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("Mật khẩu không chính xác!");
        }
        return user;
    }

    @Transactional
    public User updateUser(Long id, UserUpdateRequest request) {
        User user = findUser(id);
        String username = request.getUsername().trim();
        String email = request.getEmail().trim();
        String role = request.getRole().trim().toUpperCase(Locale.ROOT);

        if (userRepository.existsByUsernameAndIdNot(username, id)) {
            throw new IllegalStateException("Username already exists");
        }
        if (userRepository.existsByEmailAndIdNot(email, id)) {
            throw new IllegalStateException("Email already exists");
        }
        if (!ALLOWED_ROLES.contains(role)) {
            throw new IllegalArgumentException("Role must be ADMIN or USER");
        }
        if ("ADMIN".equalsIgnoreCase(user.getRole())
                && !"ADMIN".equals(role)
                && userRepository.countByRoleIgnoreCase("ADMIN") <= 1) {
            throw new IllegalStateException("At least one administrator account must remain");
        }

        user.setUsername(username);
        user.setEmail(email);
        user.setRole(role);
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id, String currentUsername) {
        User user = findUser(id);
        if (user.getUsername().equals(currentUsername)) {
            throw new IllegalStateException("You cannot delete the account currently signed in");
        }
        if ("ADMIN".equalsIgnoreCase(user.getRole())
                && userRepository.countByRoleIgnoreCase("ADMIN") <= 1) {
            throw new IllegalStateException("At least one administrator account must remain");
        }
        userRepository.delete(user);
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
    }
}
