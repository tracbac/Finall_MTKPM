package com.library.digital_library.model.model2;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO yêu cầu import sách tự động từ Google Books API (Thành viên 2).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoogleImportRequest2 {

    private String query;
    private String isbn;
    private String itemType; // EBOOK, AUDIOBOOK, PHYSICAL (mặc định EBOOK)
    private Integer quantity; // Số lượng sách nhập kho (mặc định 10)
    private BigDecimal customRentalPrice;
    private BigDecimal customCoverPrice;

    // Tùy chọn mở rộng
    private String fileFormat;
    private String narrator;
    private String shelfLocation;
}

