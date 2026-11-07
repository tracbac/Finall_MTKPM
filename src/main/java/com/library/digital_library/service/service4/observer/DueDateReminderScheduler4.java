package com.library.digital_library.service.service4.observer;

import com.library.digital_library.model.BorrowTransaction;
import com.library.digital_library.model.model3.LoanRecord3;
import com.library.digital_library.model.model3.LoanStatus3;
import com.library.digital_library.repository.BorrowTransactionRepository;
import com.library.digital_library.repository.repository3.LoanRecord3Repository;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * CONCRETE SUBJECT (Observer Pattern - Thành viên 4)
 * Mỗi sáng quét các phiếu mượn sắp đến hạn và phát sự kiện DUE_SOON.
 * Lớp này không biết thông báo được lưu/gửi như thế nào; việc đó do các Observer đảm nhận.
 */
@Component
public class DueDateReminderScheduler4 {

    private static final Logger log = LoggerFactory.getLogger(DueDateReminderScheduler4.class);
    private static final ZoneId LIBRARY_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String BORROWED = "BORROWED";
    private static final List<LoanStatus3> ACTIVE_LOAN_STATUSES = List.of(LoanStatus3.BORROWED, LoanStatus3.EXTENDED);

    private final BorrowTransactionRepository borrowTransactionRepository;
    private final LoanRecord3Repository loanRecordRepository;
    private final ApplicationEventPublisher eventPublisher;

    public DueDateReminderScheduler4(
            BorrowTransactionRepository borrowTransactionRepository,
            LoanRecord3Repository loanRecordRepository,
            ApplicationEventPublisher eventPublisher) {
        this.borrowTransactionRepository = borrowTransactionRepository;
        this.loanRecordRepository = loanRecordRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Chạy tự động lúc 08:00 mỗi ngày (giờ Việt Nam), nhắc các sách phải trả vào ngày mai.
     * Có thể đổi lịch qua thuộc tính app.notifications.due-reminder-cron.
     */
    @Scheduled(cron = "${app.notifications.due-reminder-cron:0 0 8 * * *}", zone = "Asia/Ho_Chi_Minh")
    public void remindLoansDueTomorrow() {
        int sent = runForTomorrow();
        log.info("[Observer Pattern] Đã phát {} sự kiện nhắc hạn trả sách.", sent);
    }

    /** Nhắc các phiếu mượn đến hạn vào ngày mai; dùng chung cho lịch tự động và nút chạy thủ công khi demo. */
    public int runForTomorrow() {
        return remindLoansDueOn(LocalDate.now(LIBRARY_ZONE).plusDays(1));
    }

    /**
     * Phát sự kiện DUE_SOON cho mọi phiếu mượn đang hoạt động có hạn trả đúng ngày dueDate.
     *
     * @return số sự kiện đã phát
     */
    @Transactional(readOnly = true)
    public int remindLoansDueOn(LocalDate dueDate) {
        int sent = 0;

        for (BorrowTransaction transaction : borrowTransactionRepository.findByStatusAndDueDate(BORROWED, dueDate)) {
            publish(transaction.getUser().getId(), transaction.getBook().getTitle(), dueDate);
            sent++;
        }

        for (LoanRecord3 loan : loanRecordRepository.findByDueDateAndStatusIn(dueDate, ACTIVE_LOAN_STATUSES)) {
            publish(loan.getUser().getId(), loan.getBook().getTitle(), dueDate);
            sent++;
        }

        return sent;
    }

    private void publish(Long userId, String bookTitle, LocalDate dueDate) {
        eventPublisher.publishEvent(LibraryEvent4.toUser(LibraryEventType4.DUE_SOON, userId,
                "⏰ Nhắc hạn: Sách \"" + bookTitle + "\" đến hạn trả vào ngày "
                        + dueDate.format(DATE_FORMAT) + ". Vui lòng trả đúng hạn để tránh phí phạt."));
    }
}
