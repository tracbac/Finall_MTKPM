package com.library.digital_library.service.service2.adapter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO chuẩn hóa dữ liệu sách lấy từ các nhà cung cấp bên ngoài (Target DTO trong Adapter Pattern).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExternalBookDto2 {

    private String googleVolumeId;
    private String title;
    private String author;
    private String category;
    private String isbn;
    private String description;
    private String imageUrl;
    private Integer pageCount;
    private BigDecimal estimatedCoverPrice;
    private BigDecimal estimatedRentalPrice;
    private String previewLink;
    private String publisher;
    private String publishedDate;
}

