package com.library.digital_library.model.model3;

import lombok.Data;

@Data
public class BorrowRequest3 {
    private Long userId;
    private Long bookId;
    private Integer loanDays; // Số ngày mượn mong muốn (mặc định 14 ngày nếu null)
}
