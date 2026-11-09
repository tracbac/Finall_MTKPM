package com.library.digital_library.service;

import com.library.digital_library.model.User;
import com.library.digital_library.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

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
}