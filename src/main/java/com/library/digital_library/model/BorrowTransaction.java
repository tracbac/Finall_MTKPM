package com.library.digital_library.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Data
@Entity
@Table(name = "borrow_transactions")
public class BorrowTransaction {
    private static final double DAILY_FINE_RATE = 5000.0;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    private LocalDate borrowDate;
    private LocalDate dueDate;
    private LocalDate returnDate;

    private Double fineAmount = 0.0;

    private Integer renewalCount = 0;

    // Trạng thái: PENDING (Chờ duyệt), BORROWED (Đang mượn), RETURNED (Đã trả), CANCELED (Hủy)
    @Column(nullable = false)
    private String status;

    public double getCurrentFineAmount() {
        double recordedFine = fineAmount == null ? 0.0 : fineAmount;
        if (!"BORROWED".equals(status) || dueDate == null || !LocalDate.now().isAfter(dueDate)) {
            return recordedFine;
        }
        long overdueDays = ChronoUnit.DAYS.between(dueDate, LocalDate.now());
        return Math.max(recordedFine, overdueDays * DAILY_FINE_RATE);
    }
}