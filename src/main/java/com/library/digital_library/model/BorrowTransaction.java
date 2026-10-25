package com.library.digital_library.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "borrow_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BorrowTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Khóa ngoại liên kết với bảng User
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Khóa ngoại liên kết với bảng Book
    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    private LocalDate borrowDate;
    private LocalDate dueDate; // Ngày hẹn trả
    private LocalDate returnDate; // Ngày trả thực tế
    private String status; // Trạng thái: "BORROWED", "RETURNED"
}