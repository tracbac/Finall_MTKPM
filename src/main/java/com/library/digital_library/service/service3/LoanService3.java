package com.library.digital_library.service.service3;

import com.library.digital_library.model.Book;
import com.library.digital_library.model.User;
import com.library.digital_library.model.model3.*;
import com.library.digital_library.repository.BookRepository;
import com.library.digital_library.repository.UserRepository;
import com.library.digital_library.repository.repository3.FineReceipt3Repository;
import com.library.digital_library.repository.repository3.LoanRecord3Repository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

/**
 * Service xử lý vòng đời mượn/trả, gia hạn, phạt quá hạn (Thành viên 3)
 */
@Service
@RequiredArgsConstructor
public class LoanService3 {

    private final LoanRecord3Repository loanRecordRepository;
    private final FineReceipt3Repository fineReceiptRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    public static final double DAILY_FINE_RATE = 5000.0; // 5.000 VNĐ / ngày trễ hạn
    public static final int DEFAULT_LOAN_DAYS = 14;     // Mượn mặc định 14 ngày
    public static final int DEFAULT_RENEW_DAYS = 7;     // Gia hạn thêm 7 ngày
    public static final int MAX_RENEWALS = 2;           // Tối đa 2 lần gia hạn

    /**
     * 1. MƯỢN SÁCH (Borrow Book)
     * Kiểm tra điều kiện người dùng (không nợ phạt, không có sách quá hạn), kiểm tra số lượng sách
     */
    @Transactional
    public LoanRecord3 borrowBook(BorrowRequest3 request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + request.getUserId()));

        // Kiểm tra chặn mượn: nếu user đang nợ phạt quá hạn chưa trả
        long unpaidFines = fineReceiptRepository.countByUserIdAndIsPaidFalse(user.getId());
        if (unpaidFines > 0) {
            throw new IllegalStateException("Người dùng đang có " + unpaidFines + " khoản phạt quá hạn chưa thanh toán. Không được phép mượn thêm sách!");
        }

        // Kiểm tra chặn mượn: nếu user đang giữ sách quá hạn chưa hoàn trả
        long overdueLoans = loanRecordRepository.countByUserIdAndStatus(user.getId(), LoanStatus3.OVERDUE);
        if (overdueLoans > 0) {
            throw new IllegalStateException("Người dùng đang có sách mượn quá hạn chưa trả. Vui lòng hoàn trả và giải quyết quá hạn trước!");
        }

        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sách với ID: " + request.getBookId()));

        if (book.getQuantity() == null || book.getQuantity() <= 0) {
            throw new IllegalStateException("Sách '" + book.getTitle() + "' hiện đã hết trong kho!");
        }

        // Giảm số lượng tồn kho sách
        book.setQuantity(book.getQuantity() - 1);
        bookRepository.save(book);

        int loanDays = (request.getLoanDays() != null && request.getLoanDays() > 0) ? request.getLoanDays() : DEFAULT_LOAN_DAYS;
        LocalDate today = LocalDate.now();

        LoanRecord3 loanRecord = LoanRecord3.builder()
                .user(user)
                .book(book)
                .borrowDate(today)
                .dueDate(today.plusDays(loanDays))
                .renewalCount(0)
                .maxRenewals(MAX_RENEWALS)
                .fineAmount(0.0)
                .finePaid(false)
                .status(LoanStatus3.BORROWED)
                .notes("Mượn thành công. Hạn trả: " + today.plusDays(loanDays))
                .build();

        return loanRecordRepository.save(loanRecord);
    }

    /**
     * 2. TRẢ SÁCH (Return Book)
     * Cập nhật ngày trả, trả lại số lượng vào kho, tự động tính phạt nếu quá hạn
     */
    @Transactional
    public LoanRecord3 returnBook(Long loanId) {
        LoanRecord3 loan = loanRecordRepository.findById(loanId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu mượn ID: " + loanId));

        if (loan.getStatus() == LoanStatus3.RETURNED) {
            throw new IllegalStateException("Phiếu mượn này đã được trả trước đó!");
        }

        LocalDate today = LocalDate.now();
        loan.setReturnDate(today);

        // Tăng lại số lượng sách trong kho
        Book book = loan.getBook();
        book.setQuantity(book.getQuantity() + 1);
        bookRepository.save(book);

        // Kiểm tra trả quá hạn
        if (today.isAfter(loan.getDueDate())) {
            long overdueDays = ChronoUnit.DAYS.between(loan.getDueDate(), today);
            double fineAmount = overdueDays * DAILY_FINE_RATE;

            loan.setFineAmount(fineAmount);
            loan.setFinePaid(false);
            loan.setStatus(LoanStatus3.RETURNED);
            loan.setNotes("Trả trễ " + overdueDays + " ngày. Phí phạt: " + fineAmount + " VNĐ.");

            // Lập biên lai phạt
            FineReceipt3 fine = FineReceipt3.builder()
                    .loanRecord(loan)
                    .user(loan.getUser())
                    .overdueDays((int) overdueDays)
                    .dailyRate(DAILY_FINE_RATE)
                    .totalFine(fineAmount)
                    .isPaid(false)
                    .createdAt(today)
                    .build();
            fineReceiptRepository.save(fine);
        } else {
            loan.setStatus(LoanStatus3.RETURNED);
            loan.setNotes("Đã trả sách đúng hạn.");
        }

        return loanRecordRepository.save(loan);
    }

    /**
     * 3. GIA HẠN MƯỢN (Renew Loan)
     * Kiểm tra điều kiện: chưa quá hạn, chưa vượt quá số lần gia hạn cho phép (tối đa 2 lần)
     */
    @Transactional
    public LoanRecord3 renewLoan(Long loanId, RenewRequest3 request) {
        LoanRecord3 loan = loanRecordRepository.findById(loanId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu mượn ID: " + loanId));

        if (loan.getStatus() == LoanStatus3.RETURNED) {
            throw new IllegalStateException("Phiếu mượn đã hoàn tất trả sách, không thể gia hạn!");
        }

        LocalDate today = LocalDate.now();

        // Không cho phép gia hạn nếu đã quá hạn
        if (today.isAfter(loan.getDueDate()) || loan.getStatus() == LoanStatus3.OVERDUE) {
            loan.setStatus(LoanStatus3.OVERDUE);
            loanRecordRepository.save(loan);
            throw new IllegalStateException("Sách đã quá hạn trả! Vui lòng trả sách và nộp phạt quá hạn, không thể gia hạn.");
        }

        // Kiểm tra giới hạn số lần gia hạn
        if (loan.getRenewalCount() >= loan.getMaxRenewals()) {
            throw new IllegalStateException("Đã vượt quá số lần gia hạn cho phép (Tối đa " + loan.getMaxRenewals() + " lần)!");
        }

        int extraDays = (request != null && request.getExtraDays() != null && request.getExtraDays() > 0)
                ? request.getExtraDays() : DEFAULT_RENEW_DAYS;

        loan.setDueDate(loan.getDueDate().plusDays(extraDays));
        loan.setRenewalCount(loan.getRenewalCount() + 1);
        loan.setStatus(LoanStatus3.EXTENDED);
        loan.setNotes("Gia hạn lần " + loan.getRenewalCount() + " thêm " + extraDays + " ngày. Hạn mới: " + loan.getDueDate());

        return loanRecordRepository.save(loan);
    }

    /**
     * 4. RÀ SOÁT & CẬP NHẬT TRẠNG THÁI QUÁ HẠN (Check Overdue Loans)
     */
    @Transactional
    public List<LoanRecord3> checkAndTriggerOverdueFines() {
        LocalDate today = LocalDate.now();
        List<LoanRecord3> overdueLoans = loanRecordRepository.findByDueDateBeforeAndStatusIn(
                today,
                Arrays.asList(LoanStatus3.BORROWED, LoanStatus3.EXTENDED)
        );

        for (LoanRecord3 loan : overdueLoans) {
            loan.setStatus(LoanStatus3.OVERDUE);
            long overdueDays = ChronoUnit.DAYS.between(loan.getDueDate(), today);
            double estimatedFine = overdueDays * DAILY_FINE_RATE;
            loan.setFineAmount(estimatedFine);
            loan.setNotes("Quá hạn " + overdueDays + " ngày. Tiền phạt ước tính: " + estimatedFine + " VNĐ.");
            loanRecordRepository.save(loan);
        }
        return overdueLoans;
    }

    /**
     * 5. THANH TOÁN TIỀN PHẠT (Pay Fine)
     */
    @Transactional
    public FineReceipt3 payFine(Long fineId, FinePaymentRequest3 request) {
        FineReceipt3 fine = fineReceiptRepository.findById(fineId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy biên lai phạt ID: " + fineId));

        if (Boolean.TRUE.equals(fine.getIsPaid())) {
            throw new IllegalStateException("Khoản phạt này đã được thanh toán trước đó!");
        }

        String paymentMethod = (request != null && request.getPaymentMethod() != null)
                ? request.getPaymentMethod() : "CASH";

        fine.setIsPaid(true);
        fine.setPaidDate(LocalDate.now());
        fine.setPaymentMethod(paymentMethod);

        FineReceipt3 updatedFine = fineReceiptRepository.save(fine);

        // Kiểm tra cập nhật lại trạng thái nộp phạt của LoanRecord
        LoanRecord3 loan = fine.getLoanRecord();
        if (loan != null) {
            List<FineReceipt3> userUnpaidForThisLoan = fineReceiptRepository.findByLoanRecordId(loan.getId());
            boolean allPaid = userUnpaidForThisLoan.stream().allMatch(f -> Boolean.TRUE.equals(f.getIsPaid()));
            if (allPaid) {
                loan.setFinePaid(true);
                loanRecordRepository.save(loan);
            }
        }

        return updatedFine;
    }

    // Các hàm truy vấn hỗ trợ
    public List<LoanRecord3> getAllLoans() {
        return loanRecordRepository.findAll();
    }

    public List<LoanRecord3> getLoansByUserId(Long userId) {
        return loanRecordRepository.findByUserId(userId);
    }

    public List<LoanRecord3> getOverdueLoans() {
        return loanRecordRepository.findByStatus(LoanStatus3.OVERDUE);
    }

    public List<FineReceipt3> getFinesByUserId(Long userId) {
        return fineReceiptRepository.findByUserId(userId);
    }

    public List<FineReceipt3> getUnpaidFinesByUserId(Long userId) {
        return fineReceiptRepository.findByUserIdAndIsPaidFalse(userId);
    }
}
