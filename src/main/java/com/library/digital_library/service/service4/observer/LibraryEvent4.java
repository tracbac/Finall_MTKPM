package com.library.digital_library.service.service4.observer;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * EVENT (Observer Pattern - Thành viên 4)
 * Đối tượng mang thông tin trạng thái mà Subject gửi tới tất cả Observer đã đăng ký.
 *
 * Người nhận là MỘT trong hai: một người dùng cụ thể (userId) hoặc toàn bộ tài khoản
 * thuộc một vai trò (role, ví dụ "ADMIN", "USER").
 */
public record LibraryEvent4(
        LibraryEventType4 type,
        Long userId,
        String role,
        String message,
        LocalDateTime occurredAt) {

    public LibraryEvent4 {
        Objects.requireNonNull(type, "type is required");
        if ((userId == null) == (role == null || role.isBlank())) {
            throw new IllegalArgumentException("Exactly one recipient (userId or role) is required");
        }
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("message is required");
        }
        if (occurredAt == null) {
            occurredAt = LocalDateTime.now();
        }
    }

    /** Sự kiện gửi tới một người dùng cụ thể. */
    public static LibraryEvent4 toUser(LibraryEventType4 type, Long userId, String message) {
        return new LibraryEvent4(type, userId, null, message, LocalDateTime.now());
    }

    /** Sự kiện gửi tới tất cả tài khoản thuộc một vai trò. */
    public static LibraryEvent4 toRole(LibraryEventType4 type, String role, String message) {
        return new LibraryEvent4(type, null, role, message, LocalDateTime.now());
    }

    /** Sự kiện "có sách mới" gửi tới toàn bộ độc giả (vai trò USER). */
    public static LibraryEvent4 newBook(String title, String author) {
        String byAuthor = (author == null || author.isBlank()) ? "" : " của tác giả " + author.trim();
        return toRole(LibraryEventType4.NEW_BOOK, "USER",
                "📚 Sách mới: \"" + title + "\"" + byAuthor + " vừa được thêm vào thư viện.");
    }

    public boolean isForRole() {
        return role != null;
    }

    public String recipient() {
        return isForRole() ? "role:" + role : "user:" + userId;
    }
}
