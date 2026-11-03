package com.library.digital_library.controller;

import com.library.digital_library.dto.NotificationDispatchResponse;
import com.library.digital_library.dto.NotificationUpdateResponse;
import com.library.digital_library.dto.SendNotificationRequest;
import com.library.digital_library.model.Notification;
import com.library.digital_library.service.NotificationService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/mine")
    public ResponseEntity<List<Notification>> getMyNotifications(Principal principal) {
        return ResponseEntity.ok(notificationService.getForUser(principal.getName()));
    }

    @PatchMapping("/mine/{notificationId}/read")
    public ResponseEntity<Notification> markRead(
            @PathVariable Long notificationId,
            Principal principal) {
        return ResponseEntity.ok(notificationService.markRead(principal.getName(), notificationId));
    }

    @PatchMapping("/mine/read-all")
    public ResponseEntity<NotificationUpdateResponse> markAllRead(Principal principal) {
        return ResponseEntity.ok(new NotificationUpdateResponse(
                notificationService.markAllRead(principal.getName())));
    }

    @GetMapping("/admin")
    public ResponseEntity<List<Notification>> getLatestForAdmin() {
        return ResponseEntity.ok(notificationService.getLatestForAdmin());
    }

    @PostMapping("/admin/send")
    public ResponseEntity<NotificationDispatchResponse> sendFromAdmin(
            @Valid @RequestBody SendNotificationRequest request) {
        return ResponseEntity.ok(new NotificationDispatchResponse(
                notificationService.sendFromAdmin(request)));
    }
}