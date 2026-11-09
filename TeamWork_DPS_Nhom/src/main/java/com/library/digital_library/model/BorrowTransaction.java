package com.library.digital_library.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "borrow_transactions")
public class BorrowTransaction {
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

    // Trạng thái: PENDING (Chờ duyệt), BORROWED (Đang mượn), RETURNED (Đã trả), CANCELED (Hủy)
    @Column(nullable = false)
    private String status;
}