package com.library.digital_library.model.model3;

import com.library.digital_library.model.Book;
import com.library.digital_library.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Quản lý vòng đời mượn/trả, gia hạn, phạt quá hạn (Thành viên 3)
 */
@Entity
@Table(name = "loan_records_3")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanRecord3 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(nullable = false)
    private LocalDate borrowDate;

    @Column(nullable = false)
    private LocalDate dueDate;

    private LocalDate returnDate;

    @Builder.Default
    private Integer renewalCount = 0; // Số lần đã gia hạn

    @Builder.Default
    private Integer maxRenewals = 2; // Số lần gia hạn tối đa cho phép

    @Builder.Default
    private Double fineAmount = 0.0; // Số tiền phạt nếu quá hạn

    @Builder.Default
    private Boolean finePaid = false; // Trạng thái đã nộp phạt hay chưa

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanStatus3 status;

    private String notes;
}
