package com.library.digital_library.dto;

import com.library.digital_library.model.BorrowTransaction;
import java.time.LocalDate;

public record BorrowTransactionResponse(
        Long id,
        Long userId,
        String username,
        Long bookId,
        String bookTitle,
        LocalDate borrowDate,
        LocalDate dueDate,
        LocalDate returnDate,
        String status) {

    public static BorrowTransactionResponse from(BorrowTransaction transaction) {
        return new BorrowTransactionResponse(
                transaction.getId(),
                transaction.getUser().getId(),
                transaction.getUser().getUsername(),
                transaction.getBook().getId(),
                transaction.getBook().getTitle(),
                transaction.getBorrowDate(),
                transaction.getDueDate(),
                transaction.getReturnDate(),
                transaction.getStatus());
    }
}
