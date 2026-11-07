package com.library.digital_library.model.model3;

import lombok.Data;

@Data
public class RenewRequest3 {
    private Integer extraDays; // Số ngày gia hạn thêm (mặc định 7 ngày nếu null)
}
