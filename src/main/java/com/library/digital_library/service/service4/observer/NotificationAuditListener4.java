package com.library.digital_library.service.service4.observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * CONCRETE OBSERVER #2 (Observer Pattern - Thành viên 4)
 * Ghi nhật ký kiểm toán cho mọi sự kiện nghiệp vụ.
 *
 * Minh họa "một Subject - nhiều Observer": thêm Observer này không cần sửa bất kỳ service nào.
 * Muốn gửi Email sau này chỉ cần viết thêm EmailNotificationListener4 theo cùng cách (OCP).
 */
@Component
public class NotificationAuditListener4 {

    private static final Logger log = LoggerFactory.getLogger(NotificationAuditListener4.class);

    @EventListener
    public void onLibraryEvent(LibraryEvent4 event) {
        log.info("[Observer Pattern] {} -> {} | {}", event.type(), event.recipient(), event.message());
    }
}
