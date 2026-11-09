package com.library.digital_library.model.model3;

/**
 * Trạng thái của giao dịch mượn trả (Thành viên 3 - Slice: Mượn/Trả & Bản quyền số)
 */
public enum LoanStatus3 {
    BORROWED,   // Đang mượn
    EXTENDED,   // Đã gia hạn
    RETURNED,   // Đã trả sách
    OVERDUE     // Quá hạn trả
}
