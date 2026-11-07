package com.library.digital_library.unittest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.library.digital_library.dto.SendNotificationRequest;
import com.library.digital_library.model.Notification;
import com.library.digital_library.model.User;
import com.library.digital_library.repository.NotificationRepository;
import com.library.digital_library.repository.UserRepository;
import com.library.digital_library.service.NotificationService;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NotificationService notificationService;

    private User user;

    @BeforeEach
    void setUp() {
        user = user(7L, "reader", "USER");
    }

    @Test
    void listsNotificationsForAuthenticatedUserOnly() {
        when(userRepository.findByUsername("reader")).thenReturn(Optional.of(user));
        when(notificationRepository.findTop100ByUserIdOrderByCreatedAtDesc(7L))
                .thenReturn(List.of(notification(3L, 7L)));

        List<Notification> result = notificationService.getForUser("reader");

        assertEquals(1, result.size());
        verify(notificationRepository).findTop100ByUserIdOrderByCreatedAtDesc(7L);
    }

    @Test
    void marksOnlyOwnedNotificationAsRead() {
        Notification notification = notification(3L, 7L);
        when(userRepository.findByUsername("reader")).thenReturn(Optional.of(user));
        when(notificationRepository.findByIdAndUserId(3L, 7L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(notification)).thenReturn(notification);

        Notification result = notificationService.markRead("reader", 3L);

        assertTrue(result.getIsRead());
        verify(notificationRepository).save(notification);
    }

    @Test
    void cannotReadAnotherUsersNotification() {
        when(userRepository.findByUsername("reader")).thenReturn(Optional.of(user));
        when(notificationRepository.findByIdAndUserId(3L, 7L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> notificationService.markRead("reader", 3L));
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void marksAllUnreadNotificationsAsRead() {
        List<Notification> unread = List.of(notification(3L, 7L), notification(4L, 7L));
        when(userRepository.findByUsername("reader")).thenReturn(Optional.of(user));
        when(notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(7L)).thenReturn(unread);

        int updated = notificationService.markAllRead("reader");

        assertEquals(2, updated);
        assertTrue(unread.stream().allMatch(Notification::getIsRead));
        verify(notificationRepository).saveAll(unread);
    }

    @Test
    void sendsNotificationToSelectedMember() {
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));

        int recipients = notificationService.sendFromAdmin(new SendNotificationRequest(7L, "  Tin mới  "));

        assertEquals(1, recipients);
        ArgumentCaptor<Iterable<Notification>> captor = ArgumentCaptor.forClass(Iterable.class);
        verify(notificationRepository).saveAll(captor.capture());
        Notification saved = captor.getValue().iterator().next();
        assertEquals(7L, saved.getUserId());
        assertEquals("Tin mới", saved.getMessage());
        assertEquals(false, saved.getIsRead());
    }

    @Test
    void broadcastsOnlyToMemberAccounts() {
        User otherMember = user(8L, "another-reader", "USER");
        when(userRepository.findByRoleIgnoreCase("USER")).thenReturn(List.of(user, otherMember));

        int recipients = notificationService.sendFromAdmin(new SendNotificationRequest(null, "Thông báo chung"));

        assertEquals(2, recipients);
        ArgumentCaptor<Iterable<Notification>> captor = ArgumentCaptor.forClass(Iterable.class);
        verify(notificationRepository).saveAll(captor.capture());
        assertEquals(List.of(7L, 8L), toList(captor.getValue()).stream().map(Notification::getUserId).toList());
    }

    @Test
    void rejectsBlankMessagesAndBroadcastWhenThereAreNoMembers() {
        assertThrows(IllegalArgumentException.class,
                () -> notificationService.sendFromAdmin(new SendNotificationRequest(7L, "  ")));
        when(userRepository.findByRoleIgnoreCase("USER")).thenReturn(List.of());
        assertThrows(IllegalStateException.class,
                () -> notificationService.sendFromAdmin(new SendNotificationRequest(null, "Hello")));
        verify(notificationRepository, never()).saveAll(any());
    }

    private User user(Long id, String username, String role) {
        User target = new User();
        target.setId(id);
        target.setUsername(username);
        target.setRole(role);
        return target;
    }

    private Notification notification(Long id, Long userId) {
        Notification notification = new Notification();
        notification.setId(id);
        notification.setUserId(userId);
        notification.setMessage("Tin mới");
        notification.setIsRead(false);
        return notification;
    }

    private List<Notification> toList(Iterable<Notification> notifications) {
        java.util.ArrayList<Notification> result = new java.util.ArrayList<>();
        notifications.forEach(result::add);
        return result;
    }
}
