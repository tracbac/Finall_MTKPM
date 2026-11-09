package com.library.digital_library.unittest.unittest4;

import com.library.digital_library.dto.AdminBookRequest;
import com.library.digital_library.model.Book;
import com.library.digital_library.model.BorrowTransaction;
import com.library.digital_library.model.User;
import com.library.digital_library.model.model3.LoanRecord3;
import com.library.digital_library.model.model3.LoanStatus3;
import com.library.digital_library.repository.BookRepository;
import com.library.digital_library.repository.BorrowTransactionRepository;
import com.library.digital_library.repository.repository3.LoanRecord3Repository;
import com.library.digital_library.service.BookCoverStorage;
import com.library.digital_library.service.BookPriceService;
import com.library.digital_library.service.BookService;
import com.library.digital_library.service.NotificationService;
import com.library.digital_library.service.service4.observer.DueDateReminderScheduler4;
import com.library.digital_library.service.service4.observer.InAppNotificationListener4;
import com.library.digital_library.service.service4.observer.LibraryEvent4;
import com.library.digital_library.service.service4.observer.LibraryEventType4;
import com.library.digital_library.service.service4.observer.NotificationAuditListener4;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static com.library.digital_library.unittest.unittest4.LibraryEventMatchers4.roleEvent;
import static com.library.digital_library.unittest.unittest4.LibraryEventMatchers4.userEvent;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Kiểm thử Design Pattern: Observer (Thông báo tự động theo sự kiện - Thành viên 4)")
class ObserverPattern4Test {

    @Mock
    private NotificationService notificationService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private BorrowTransactionRepository borrowTransactionRepository;

    @Mock
    private LoanRecord3Repository loanRecordRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookPriceService bookPriceService;

    @Mock
    private BookCoverStorage bookCoverStorage;

    // ------------------------------------------------------------------
    // 1. EVENT
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Sự kiện phải có đúng một người nhận: userId hoặc role")
    void eventRequiresExactlyOneRecipient() {
        assertThrows(IllegalArgumentException.class,
                () -> new LibraryEvent4(LibraryEventType4.NEW_BOOK, null, null, "msg", null));
        assertThrows(IllegalArgumentException.class,
                () -> new LibraryEvent4(LibraryEventType4.NEW_BOOK, 1L, "USER", "msg", null));
        assertThrows(IllegalArgumentException.class,
                () -> LibraryEvent4.toUser(LibraryEventType4.BORROW_APPROVED, 1L, " "));
    }

    @Test
    @DisplayName("Sự kiện sách mới được gửi tới toàn bộ độc giả (vai trò USER)")
    void newBookEventTargetsAllReaders() {
        LibraryEvent4 event = LibraryEvent4.newBook("Clean Code", "Robert C. Martin");

        assertEquals(LibraryEventType4.NEW_BOOK, event.type());
        assertEquals("USER", event.role());
        assertEquals("📚 Sách mới: \"Clean Code\" của tác giả Robert C. Martin vừa được thêm vào thư viện.",
                event.message());
    }

    // ------------------------------------------------------------------
    // 2. OBSERVERS
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Observer In-App lưu thông báo cho đúng người dùng")
    void inAppObserverSavesNotificationForUser() {
        InAppNotificationListener4 listener = new InAppNotificationListener4(notificationService);

        listener.onLibraryEvent(LibraryEvent4.toUser(LibraryEventType4.BORROW_APPROVED, 7L, "Đã duyệt."));

        verify(notificationService).notifyUser(7L, "Đã duyệt.");
        verify(notificationService, never()).notifyRole(anyString(), anyString());
    }

    @Test
    @DisplayName("Observer In-App gửi thông báo tới toàn bộ tài khoản theo vai trò")
    void inAppObserverBroadcastsToRole() {
        InAppNotificationListener4 listener = new InAppNotificationListener4(notificationService);

        listener.onLibraryEvent(LibraryEvent4.toRole(LibraryEventType4.BORROW_REQUESTED, "ADMIN", "Có yêu cầu mới."));

        verify(notificationService).notifyRole("ADMIN", "Có yêu cầu mới.");
    }

    @Test
    @DisplayName("Spring tự đăng ký Observer: một sự kiện được chuyển tới mọi Observer")
    void springDeliversEventToAllRegisteredObservers() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.registerBean(NotificationService.class, () -> notificationService);
            context.registerBean(InAppNotificationListener4.class);
            context.registerBean(NotificationAuditListener4.class);
            context.refresh();

            // Subject chỉ phát sự kiện, không biết có những Observer nào
            context.publishEvent(LibraryEvent4.toUser(LibraryEventType4.LOAN_RENEWED, 3L, "Đã gia hạn."));
        }

        verify(notificationService, times(1)).notifyUser(3L, "Đã gia hạn.");
    }

    // ------------------------------------------------------------------
    // 3. SUBJECTS
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Admin thêm sách mới thì BookService phát sự kiện NEW_BOOK")
    void bookServicePublishesNewBookEvent() {
        BookService bookService = new BookService(
                bookRepository, bookPriceService, bookCoverStorage, null, eventPublisher);
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> {
            Book saved = invocation.getArgument(0);
            saved.setId(50L);
            return saved;
        });

        AdminBookRequest request = new AdminBookRequest();
        request.setTitle("Refactoring");
        request.setAuthor("Martin Fowler");
        request.setQuantity(3);
        request.setRentalPrice(new BigDecimal("10000"));
        request.setCoverPrice(new BigDecimal("150000"));

        bookService.createBook(request);

        verify(eventPublisher).publishEvent(argThat(roleEvent(LibraryEventType4.NEW_BOOK, "USER",
                "📚 Sách mới: \"Refactoring\" của tác giả Martin Fowler vừa được thêm vào thư viện.")));
    }

    @Test
    @DisplayName("Bộ nhắc hạn phát sự kiện DUE_SOON cho cả sách giấy và phiếu mượn đến hạn ngày mai")
    void reminderPublishesDueSoonEvents() {
        DueDateReminderScheduler4 scheduler = new DueDateReminderScheduler4(
                borrowTransactionRepository, loanRecordRepository, eventPublisher);
        LocalDate tomorrow = LocalDate.of(2026, 11, 9);

        BorrowTransaction transaction = new BorrowTransaction();
        transaction.setUser(user(1L));
        transaction.setBook(book("Clean Code"));
        transaction.setStatus("BORROWED");
        transaction.setDueDate(tomorrow);

        LoanRecord3 loan = LoanRecord3.builder()
                .user(user(2L))
                .book(book("Effective Java"))
                .dueDate(tomorrow)
                .status(LoanStatus3.EXTENDED)
                .build();

        when(borrowTransactionRepository.findByStatusAndDueDate("BORROWED", tomorrow)).thenReturn(List.of(transaction));
        when(loanRecordRepository.findByDueDateAndStatusIn(any(LocalDate.class), any())).thenReturn(List.of(loan));

        int sent = scheduler.remindLoansDueOn(tomorrow);

        assertEquals(2, sent);
        verify(eventPublisher).publishEvent(argThat(userEvent(LibraryEventType4.DUE_SOON, 1L,
                "⏰ Nhắc hạn: Sách \"Clean Code\" đến hạn trả vào ngày 09/11/2026. Vui lòng trả đúng hạn để tránh phí phạt.")));
        verify(eventPublisher).publishEvent(argThat(userEvent(LibraryEventType4.DUE_SOON, 2L,
                "⏰ Nhắc hạn: Sách \"Effective Java\" đến hạn trả vào ngày 09/11/2026. Vui lòng trả đúng hạn để tránh phí phạt.")));
        verify(loanRecordRepository).findByDueDateAndStatusIn(argThat(tomorrow::equals),
                argThat(statuses -> statuses.contains(LoanStatus3.BORROWED)
                        && statuses.contains(LoanStatus3.EXTENDED)
                        && statuses.size() == 2));
    }

    @Test
    @DisplayName("Không có phiếu mượn đến hạn thì không phát sự kiện nào")
    void reminderPublishesNothingWhenNoLoanIsDue() {
        DueDateReminderScheduler4 scheduler = new DueDateReminderScheduler4(
                borrowTransactionRepository, loanRecordRepository, eventPublisher);
        LocalDate tomorrow = LocalDate.of(2026, 11, 9);
        when(borrowTransactionRepository.findByStatusAndDueDate("BORROWED", tomorrow)).thenReturn(List.of());
        when(loanRecordRepository.findByDueDateAndStatusIn(any(LocalDate.class), any())).thenReturn(List.of());

        assertEquals(0, scheduler.remindLoansDueOn(tomorrow));
        verify(eventPublisher, never()).publishEvent(any(Object.class));
    }

    private static User user(Long id) {
        User user = new User();
        user.setId(id);
        user.setUsername("reader" + id);
        return user;
    }

    private static Book book(String title) {
        Book book = new Book();
        book.setTitle(title);
        return book;
    }
}
