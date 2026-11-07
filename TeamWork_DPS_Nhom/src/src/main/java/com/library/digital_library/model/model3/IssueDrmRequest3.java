package com.library.digital_library.model.model3;

import lombok.Data;

@Data
public class IssueDrmRequest3 {
    private Long userId;
    private Long bookId;
    private Integer validDays;    // Số ngày có hiệu lực (ví dụ 30 ngày)
    private Integer maxDownloads; // Số lần tải tối đa cho phép (ví dụ 3 lần)
}
