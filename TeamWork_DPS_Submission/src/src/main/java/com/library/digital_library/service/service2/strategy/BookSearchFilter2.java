package com.library.digital_library.service.service2.strategy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Filter DTO chứa các tiêu chí lọc sách nâng cao cho Strategy Pattern (Thành viên 2).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookSearchFilter2 {

    private String keyword;
    private String author;
    private String category;
    private Boolean onlyAvailable;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
}

