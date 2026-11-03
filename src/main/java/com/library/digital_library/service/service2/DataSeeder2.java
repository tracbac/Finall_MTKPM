package com.library.digital_library.service.service2;

import com.library.digital_library.model.User;
import com.library.digital_library.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Tự động khởi tạo tài khoản đăng nhập mặc định khi cơ sở dữ liệu chưa có người dùng.
 * Không nạp thêm sách mẫu để giữ nguyên kho sách sạch theo yêu cầu.
 */
@Component
public class DataSeeder2 implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder2.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder2(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            log.info("[DataSeeder2] Khởi tạo tài khoản người dùng mặc định...");

            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@library.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole("ADMIN");
            userRepository.save(admin);

            User reader = new User();
            reader.setUsername("reader");
            reader.setEmail("reader@library.com");
            reader.setPassword(passwordEncoder.encode("123456"));
            reader.setRole("USER");
            userRepository.save(reader);

            log.info("[DataSeeder2] Đã tạo 2 tài khoản: admin / admin123 (ADMIN) và reader / 123456 (USER)");
        }
    }
}

