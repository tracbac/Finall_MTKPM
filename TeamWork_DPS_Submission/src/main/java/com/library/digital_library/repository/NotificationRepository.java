package com.library.digital_library.repository;

import com.library.digital_library.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findTop100ByUserIdOrderByCreatedAtDesc(Long userId);
    List<Notification> findTop100ByOrderByCreatedAtDesc();
    Optional<Notification> findByIdAndUserId(Long id, Long userId);
    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);
    List<Notification> findByUserId(Long userId);
}