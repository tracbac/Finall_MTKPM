package com.library.digital_library.service;

import com.library.digital_library.model.Notification;
import com.library.digital_library.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


@Service
public class NotificationScheduler {

    @Autowired
    private NotificationRepository notificationRepository;

    // Chạy tự động mỗi 60 giây (60000 ms)
    @Scheduled(fixedRate = 60000)
    public void autoSendNotification() {
        System.out.println("--- Observer Pattern: Đang quét hệ thống để gửi thông báo tự động... ---");

        // Demo: Tự động gửi 1 thông báo chúc mừng cho user id = 1
        // (Trong thực tế sẽ viết logic quét phiếu mượn sắp hết hạn)
        Notification noti = new Notification();
        noti.setUserId(1L);
        noti.setMessage("Thông báo tự động: Chào mừng bạn đến với Thư viện số!");
        noti.setIsRead(false);

        notificationRepository.save(noti);
        System.out.println("--- Đã tự động gửi thông báo cho User 1 ---");
    }
}