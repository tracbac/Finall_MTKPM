package com.library.digital_library.service.service2.strategy;

import com.library.digital_library.model.Book;

import java.util.List;

/**
 * Strategy Interface trong Strategy Pattern (Thành viên 2).
 * Định nghĩa hợp đồng chung cho các thuật toán tìm kiếm và lọc danh mục sách.
 */
public interface BookSearchStrategy2 {

    /**
     * Khóa định danh chiến lược (ví dụ: TITLE, AUTHOR, CATEGORY, ADVANCED).
     */
    String getStrategyKey();

    /**
     * Thực thi thuật toán tìm kiếm/lọc trên tập dữ liệu sách.
     */
    List<Book> executeSearch(List<Book> books, BookSearchFilter2 filter);
}

