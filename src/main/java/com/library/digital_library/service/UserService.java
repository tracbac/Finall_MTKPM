package com.library.digital_library.service;

import com.library.digital_library.model.User;
import com.library.digital_library.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // 1. Lấy danh sách toàn bộ người dùng (Dành cho Admin quản lý)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // 2. Đăng ký tài khoản người dùng mới
    public User registerUser(User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại trong hệ thống!");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email này đã được sử dụng!");
        }
        
        // Mặc định phân quyền nếu chưa có
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("MEMBER");
        }

        return userRepository.save(user);
    }

    // 3. Đăng nhập cơ bản
    public User login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản người dùng!"));

        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("Mật khẩu không chính xác!");
        }

        return user;
    }
}