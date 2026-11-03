package com.library.digital_library.service.service2.adapter;

import java.util.List;
import java.util.Optional;

/**
 * Target Interface trong Adapter Pattern.
 * Định nghĩa hợp đồng chuẩn hóa mà hệ thống nội bộ (Client) mong muốn để tương tác với các nguồn sách ngoại vi.
 */
public interface ExternalBookCatalogPort2 {

    /**
     * Tra cứu danh sách sách từ nguồn ngoài theo từ khóa.
     */
    List<ExternalBookDto2> searchBooks(String query, int maxResults);

    /**
     * Tra cứu chi tiết một cuốn sách từ nguồn ngoài theo mã định danh chuẩn quốc tế ISBN.
     */
    Optional<ExternalBookDto2> findBookByIsbn(String isbn);
}

