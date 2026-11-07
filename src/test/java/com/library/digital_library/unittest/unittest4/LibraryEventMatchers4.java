package com.library.digital_library.unittest.unittest4;

import com.library.digital_library.service.service4.observer.LibraryEvent4;
import com.library.digital_library.service.service4.observer.LibraryEventType4;
import java.util.Objects;
import org.mockito.ArgumentMatcher;

/**
 * Bộ so khớp Mockito dùng chung để kiểm tra sự kiện mà Subject đã phát (Observer Pattern - Thành viên 4).
 * Dùng: verify(eventPublisher).publishEvent(argThat(userEvent(LibraryEventType4.BORROW_APPROVED, 1L, "...")));
 */
public final class LibraryEventMatchers4 {

    private LibraryEventMatchers4() {
    }

    public static ArgumentMatcher<Object> userEvent(LibraryEventType4 type, Long userId, String message) {
        return new ArgumentMatcher<>() {
            @Override
            public boolean matches(Object argument) {
                return argument instanceof LibraryEvent4 event
                        && event.type() == type
                        && Objects.equals(event.userId(), userId)
                        && Objects.equals(event.message(), message);
            }

            @Override
            public String toString() {
                return "LibraryEvent4[" + type + " -> user:" + userId + " | " + message + "]";
            }
        };
    }

    public static ArgumentMatcher<Object> roleEvent(LibraryEventType4 type, String role, String message) {
        return new ArgumentMatcher<>() {
            @Override
            public boolean matches(Object argument) {
                return argument instanceof LibraryEvent4 event
                        && event.type() == type
                        && Objects.equals(event.role(), role)
                        && Objects.equals(event.message(), message);
            }

            @Override
            public String toString() {
                return "LibraryEvent4[" + type + " -> role:" + role + " | " + message + "]";
            }
        };
    }
}
