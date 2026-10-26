package com.library.digital_library.controller.controller3;

import com.library.digital_library.model.model3.*;
import com.library.digital_library.service.service3.LoanService3;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller Quản lý vòng đời mượn/trả, gia hạn, phạt quá hạn (Thành viên 3)
 */
@RestController
@RequestMapping("/api/v3/loans")
@RequiredArgsConstructor
public class LoanController3 {

    private final LoanService3 loanService;

    /**
     * API 1: Mượn sách
     */
    @PostMapping("/borrow")
    public ResponseEntity<?> borrowBook(@RequestBody BorrowRequest3 request) {
        try {
            LoanRecord3 loan = loanService.borrowBook(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(loan);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi hệ thống: " + e.getMessage());
        }
    }

    /**
     * API 2: Trả sách & tính phạt nếu có quá hạn
     */
    @PostMapping("/{id}/return")
    public ResponseEntity<?> returnBook(@PathVariable Long id) {
        try {
            LoanRecord3 loan = loanService.returnBook(id);
            return ResponseEntity.ok(loan);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * API 3: Gia hạn mượn sách
     */
    @PostMapping("/{id}/renew")
    public ResponseEntity<?> renewLoan(@PathVariable Long id, @RequestBody(required = false) RenewRequest3 request) {
        try {
            LoanRecord3 loan = loanService.renewLoan(id, request);
            return ResponseEntity.ok(loan);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * API 4: Rà soát & cập nhật các sách quá hạn
     */
    @PostMapping("/check-overdue")
    public ResponseEntity<List<LoanRecord3>> triggerOverdueCheck() {
        List<LoanRecord3> overdueLoans = loanService.checkAndTriggerOverdueFines();
        return ResponseEntity.ok(overdueLoans);
    }

    /**
     * API 5: Thanh toán tiền phạt
     */
    @PostMapping("/fines/{fineId}/pay")
    public ResponseEntity<?> payFine(@PathVariable Long fineId, @RequestBody(required = false) FinePaymentRequest3 request) {
        try {
            FineReceipt3 fine = loanService.payFine(fineId, request);
            return ResponseEntity.ok(fine);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * API 6: Lấy danh sách mượn sách của một người dùng
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<LoanRecord3>> getUserLoans(@PathVariable Long userId) {
        return ResponseEntity.ok(loanService.getLoansByUserId(userId));
    }

    /**
     * API 7: Lấy tất cả phiếu mượn
     */
    @GetMapping("/all")
    public ResponseEntity<List<LoanRecord3>> getAllLoans() {
        return ResponseEntity.ok(loanService.getAllLoans());
    }

    /**
     * API 8: Lấy danh sách phiếu mượn quá hạn
     */
    @GetMapping("/overdue")
    public ResponseEntity<List<LoanRecord3>> getOverdueLoans() {
        return ResponseEntity.ok(loanService.getOverdueLoans());
    }

    /**
     * API 9: Lấy toàn bộ biên lai phạt của user
     */
    @GetMapping("/fines/user/{userId}")
    public ResponseEntity<List<FineReceipt3>> getUserFines(@PathVariable Long userId) {
        return ResponseEntity.ok(loanService.getFinesByUserId(userId));
    }

    /**
     * API 10: Lấy danh sách khoản phạt chưa thanh toán của user
     */
    @GetMapping("/fines/user/{userId}/unpaid")
    public ResponseEntity<List<FineReceipt3>> getUnpaidFines(@PathVariable Long userId) {
        return ResponseEntity.ok(loanService.getUnpaidFinesByUserId(userId));
    }
}
