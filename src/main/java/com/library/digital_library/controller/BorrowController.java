package com.library.digital_library.controller;

import com.library.digital_library.dto.BorrowRequest;
import com.library.digital_library.dto.BorrowTransactionResponse;
import com.library.digital_library.service.BorrowTransactionService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.security.Principal;

@RestController
@RequestMapping("/api/borrows")
@RequiredArgsConstructor
public class BorrowController {

    private final BorrowTransactionService borrowTransactionService;

    @PostMapping
    public ResponseEntity<BorrowTransactionResponse> createBorrowRequest(
            @Valid @RequestBody BorrowRequest request,
            Principal principal) {
        return ResponseEntity.status(201).body(BorrowTransactionResponse.from(
                borrowTransactionService.createBorrowRequest(principal.getName(), request)));
    }

    @GetMapping("/my-history")
    public ResponseEntity<List<BorrowTransactionResponse>> getMyTransactions(Principal principal) {
        return ResponseEntity.ok(borrowTransactionService.getUserTransactionsByUsername(principal.getName())
                .stream().map(BorrowTransactionResponse::from).toList());
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<BorrowTransactionResponse> cancelBorrowRequest(
            @PathVariable Long id,
            Principal principal) {
        return ResponseEntity.ok(BorrowTransactionResponse.from(
                borrowTransactionService.cancelBorrowRequest(principal.getName(), id)));
    }

    @PutMapping("/{id}/renew")
    public ResponseEntity<BorrowTransactionResponse> renewBorrow(
            @PathVariable Long id,
            Principal principal) {
        return ResponseEntity.ok(BorrowTransactionResponse.from(
                borrowTransactionService.renewBorrow(principal.getName(), id)));
    }
}