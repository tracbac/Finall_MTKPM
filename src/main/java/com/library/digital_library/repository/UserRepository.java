package com.library.digital_library.repository;

import com.library.digital_library.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Tìm kiếm người dùng bằng tên đăng nhập (dùng cho đăng nhập)
    Optional<User> findByUsername(String username);
    
    // Kiểm tra xem email đã tồn tại chưa (dùng cho đăng ký)
    boolean existsByEmail(String email);
}