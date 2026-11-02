package com.library.digital_library.controller;

import com.library.digital_library.model.Notification;
import com.library.digital_library.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationRepository notificationRepository;

    // API 1: Gửi thông báo mới
    @PostMapping("/send")
    public ResponseEntity<Notification> sendNotification(@RequestBody Notification notification) {
        notification.setIsRead(false);
        Notification savedNoti = notificationRepository.save(notification);
        return ResponseEntity.ok(savedNoti);
    }

    // API 2: Lấy danh sách thông báo của user
    @GetMapping("/{userId}")
    public List<Notification> getNotificationsByUserId(@PathVariable Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}