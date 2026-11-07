package com.library.digital_library.service;

import com.library.digital_library.dto.SendNotificationRequest;
import com.library.digital_library.model.Notification;
import com.library.digital_library.model.User;
import com.library.digital_library.repository.NotificationRepository;
import com.library.digital_library.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final String USER_ROLE = "USER";
    private static final String ADMIN_ROLE = "ADMIN";

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<Notification> getForUser(String username) {
        User user = findUser(username);
        return notificationRepository.findTop100ByUserIdOrderByCreatedAtDesc(user.getId());
    }

    @Transactional
    public Notification markRead(String username, Long notificationId) {
        User user = findUser(username);
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, user.getId())
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy thông báo."));
        notification.setIsRead(true);
        return notificationRepository.save(notification);
    }

    @Transactional
    public int markAllRead(String username) {
        User user = findUser(username);
        List<Notification> unread = notificationRepository
                .findByUserIdAndIsReadFalseOrderByCreatedAtDesc(user.getId());
        unread.forEach(notification -> notification.setIsRead(true));
        notificationRepository.saveAll(unread);
        return unread.size();
    }

    @Transactional(readOnly = true)
    public List<Notification> getLatestForAdmin() {
        return notificationRepository.findTop100ByOrderByCreatedAtDesc();
    }

    @Transactional
    public int sendFromAdmin(SendNotificationRequest request) {
        if (request == null || !StringUtils.hasText(request.message())) {
            throw new IllegalArgumentException("Nội dung thông báo không được để trống.");
        }
        String message = request.message().trim();

        List<User> recipients = request.userId() == null
                ? userRepository.findByRoleIgnoreCase(USER_ROLE)
                : List.of(userRepository.findById(request.userId())
                        .orElseThrow(() -> new NoSuchElementException("Không tìm thấy người nhận.")));
        if (recipients.isEmpty()) {
            throw new IllegalStateException("Không có thành viên để nhận thông báo.");
        }

        notificationRepository.saveAll(recipients.stream()
                .map(user -> createNotification(user.getId(), message))
                .toList());
        return recipients.size();
    }

    @Transactional
    public void notifyUser(Long userId, String message) {
        notificationRepository.save(createNotification(userId, message));
    }

    @Transactional
    public void notifyRole(String role, String message) {
        List<Notification> notifications = userRepository.findByRoleIgnoreCase(role).stream()
                .map(user -> createNotification(user.getId(), message))
                .toList();
        if (!notifications.isEmpty()) {
            notificationRepository.saveAll(notifications);
        }
    }

    private User findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy tài khoản người dùng."));
    }

    private Notification createNotification(Long userId, String message) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setMessage(message);
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        return notification;
    }
}
