package com.library.digital_library.service.service4.observer;

import com.library.digital_library.service.NotificationService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * CONCRETE OBSERVER #1 (Observer Pattern - Thành viên 4)
 * Lắng nghe mọi LibraryEvent4 và lưu thành thông báo trong ứng dụng (hiển thị ở tab 🔔 Thông báo).
 *
 * Spring tự "đăng ký" (attach) lớp này với Subject nhờ @EventListener,
 * nên các service phát sự kiện hoàn toàn không biết đến sự tồn tại của nó.
 */
@Component
public class InAppNotificationListener4 {

    private final NotificationService notificationService;

    public InAppNotificationListener4(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @EventListener
    public void onLibraryEvent(LibraryEvent4 event) {
        if (event.isForRole()) {
            notificationService.notifyRole(event.role(), event.message());
        } else {
            notificationService.notifyUser(event.userId(), event.message());
        }
    }
}
