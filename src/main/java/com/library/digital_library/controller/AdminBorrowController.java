package com.library.digital_library.controller;

import com.library.digital_library.dto.BorrowTransactionResponse;
import com.library.digital_library.service.BorrowTransactionService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/borrows")
@RequiredArgsConstructor
public class AdminBorrowController {

    private final BorrowTransactionService borrowTransactionService;

    @GetMapping
    public ResponseEntity<List<BorrowTransactionResponse>> getAllTransactions() {
        return ResponseEntity.ok(borrowTransactionService.getAllTransactions().stream()
                .map(BorrowTransactionResponse::from).toList());
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<BorrowTransactionResponse> approveTransaction(@PathVariable Long id) {
        return ResponseEntity.ok(BorrowTransactionResponse.from(
                borrowTransactionService.approveTransaction(id)));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<BorrowTransactionResponse> rejectBorrowRequest(@PathVariable Long id) {
        return ResponseEntity.ok(BorrowTransactionResponse.from(
                borrowTransactionService.rejectBorrowRequest(id)));
    }

    @PutMapping("/{id}/return")
    public ResponseEntity<BorrowTransactionResponse> returnBook(@PathVariable Long id) {
        return ResponseEntity.ok(BorrowTransactionResponse.from(
                borrowTransactionService.returnBook(id)));
    }

    @PutMapping("/{id}/renew")
    public ResponseEntity<BorrowTransactionResponse> renewBorrow(@PathVariable Long id) {
        return ResponseEntity.ok(BorrowTransactionResponse.from(
                borrowTransactionService.renewBorrowByAdmin(id)));
    }
}