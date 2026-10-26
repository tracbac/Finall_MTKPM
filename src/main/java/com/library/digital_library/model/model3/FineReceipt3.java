package com.library.digital_library.model.model3;

import com.library.digital_library.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Biên lai phạt trễ hạn mượn sách (Thành viên 3)
 */
@Entity
@Table(name = "fine_receipts_3")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FineReceipt3 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "loan_record_id", nullable = false)
    private LoanRecord3 loanRecord;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Integer overdueDays; // Số ngày quá hạn

    @Column(nullable = false)
    private Double dailyRate; // Mức phí phạt/ngày (mặc định 5000 VNĐ/ngày)

    @Column(nullable = false)
    private Double totalFine; // Tổng tiền phạt = overdueDays * dailyRate

    @Builder.Default
    private Boolean isPaid = false; // Đã thanh toán chưa

    private LocalDate paidDate; // Ngày thanh toán

    private String paymentMethod; // Phương thức thanh toán (CASH, BANKING, WALLET)

    @Column(nullable = false)
    private LocalDate createdAt; // Ngày lập biên lai phạt
}
