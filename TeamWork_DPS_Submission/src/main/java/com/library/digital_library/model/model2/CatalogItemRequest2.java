package com.library.digital_library.model.model2;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO dùng để tạo mới hoặc import tài liệu thư viện qua Factory Method (Thành viên 2).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CatalogItemRequest2 {

    // Thuộc tính chung
    private String title;
    private String author;
    private String category;
    private String isbn;
    private Integer quantity;
    private String imageUrl;
    private String description;
    private BigDecimal coverPrice;
    private BigDecimal rentalPrice;
    private String itemType; // EBOOK, AUDIOBOOK, PHYSICAL

    // Thuộc tính mở rộng cho E-Book (DigitalDownloadable2)
    private String fileFormat;
    private Double fileSizeMb;
    private String downloadUrl;

    // Thuộc tính mở rộng cho Audio Book (AudioStreamable2)
    private Integer durationMinutes;
    private String narrator;
    private String streamUrl;

    // Thuộc tính mở rộng cho Sách giấy (PhysicalShelvable2)
    private String shelfLocation;

    // Mã Google Books nếu có
    private String googleVolumeId;
}

