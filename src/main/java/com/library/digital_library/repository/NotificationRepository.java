package com.library.digital_library.repository;

import com.library.digital_library.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // Hàm tự động lấy danh sách thông báo của 1 user
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
}