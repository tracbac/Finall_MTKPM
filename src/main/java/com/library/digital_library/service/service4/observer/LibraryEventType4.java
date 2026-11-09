package com.library.digital_library.service.service4.observer;

/**
 * Các loại sự kiện nghiệp vụ mà Subject có thể phát ra (Observer Pattern - Thành viên 4).
 */
public enum LibraryEventType4 {
    // Mượn sách giấy (BorrowTransactionService)
    BORROW_REQUESTED,
    BORROW_APPROVED,
    BORROW_REJECTED,
    BORROW_RETURNED,

    // Vòng đời phiếu mượn (LoanService3)
    LOAN_CREATED,
    LOAN_RETURNED,
    LOAN_RENEWED,
    LOAN_OVERDUE,
    FINE_PAID,

    // Yêu cầu mượn E-Book (EBookBorrowRequest3Service)
    EBOOK_REQUESTED,
    EBOOK_APPROVED,
    EBOOK_REJECTED,

    // Nghiệp vụ mới do Thành viên 4 bổ sung
    NEW_BOOK,
    DUE_SOON
}
