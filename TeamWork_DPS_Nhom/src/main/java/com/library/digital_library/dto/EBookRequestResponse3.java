package com.library.digital_library.dto;

import com.library.digital_library.model.model3.EBookBorrowRequest3;
import java.time.LocalDateTime;

public record EBookRequestResponse3(
        Long id,
        Long userId,
        String username,
        Long bookId,
        String bookTitle,
        String status,
        LocalDateTime requestedAt,
        LocalDateTime reviewedAt,
        String reviewNote,
        String licenseKey) {

    public static EBookRequestResponse3 from(EBookBorrowRequest3 request) {
        return new EBookRequestResponse3(
                request.getId(),
                request.getUser().getId(),
                request.getUser().getUsername(),
                request.getBook().getId(),
                request.getBook().getTitle(),
                request.getStatus().name(),
                request.getRequestedAt(),
                request.getReviewedAt(),
                request.getReviewNote(),
                request.getLicenseKey());
    }
}
