package com.library.digital_library.controller.controller2;

import com.library.digital_library.model.model2.CatalogBookDetailResponse2;
import com.library.digital_library.model.model2.CatalogItemRequest2;
import com.library.digital_library.model.model2.GoogleImportRequest2;
import com.library.digital_library.service.service2.BookCatalogService2;
import com.library.digital_library.service.service2.adapter.ExternalBookDto2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * REST Controller cho Lát cắt Kho sách & Tích hợp API ngoài (Thành viên 2).
 * Ánh xạ tại /api/books/v2 để khớp với cấu hình permitAll() sẵn có trong SecurityConfig.java.
 */
@RestController
@RequestMapping("/api/books/v2")
public class BookCatalogController2 {

    private final BookCatalogService2 catalogService;

    public BookCatalogController2(BookCatalogService2 catalogService) {
        this.catalogService = catalogService;
    }

    /**
     * API 1: Tìm kiếm & Lọc danh mục sách nâng cao (Áp dụng Strategy Pattern)
     * URL: GET /api/books/v2/search?keyword=...&strategy=TITLE|AUTHOR|CATEGORY|ADVANCED&onlyAvailable=false
     */
    @GetMapping("/search")
    public ResponseEntity<List<CatalogBookDetailResponse2>> searchCatalog(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean onlyAvailable,
            @RequestParam(required = false, defaultValue = "ADVANCED") String strategy) {

        List<CatalogBookDetailResponse2> result = catalogService.searchCatalog(keyword, author, category, onlyAvailable, strategy);
        return ResponseEntity.ok(result);
    }

    /**
     * API 2: Tra cứu và xem trước sách từ Google Books API (Áp dụng Adapter Pattern, hỗ trợ phân trang)
     * URL: GET /api/books/v2/google/search?q=java&page=1&maxResults=6
     */
    @GetMapping("/google/search")
    public ResponseEntity<List<ExternalBookDto2>> searchGoogleBooks(
            @RequestParam(required = false, defaultValue = "Design Patterns") String q,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "6") int maxResults) {

        List<ExternalBookDto2> books = catalogService.previewFromGoogleBooks(q, page, maxResults);
        return ResponseEntity.ok(books);
    }

    // Overload thuận tiện cho Unit Test và gọi trực tiếp
    public ResponseEntity<List<ExternalBookDto2>> searchGoogleBooks(String q, int maxResults) {
        return searchGoogleBooks(q, 1, maxResults);
    }

    /**
     * API 3: Tự động import sách từ Google Books vào kho sách nội bộ
     * (Kết hợp: Google Books API -> Adapter Pattern -> Factory Method Pattern -> Database)
     * URL: POST /api/books/v2/google/import
     */
    @PostMapping("/google/import")
    public ResponseEntity<?> importFromGoogleBooks(@RequestBody GoogleImportRequest2 request) {
        try {
            CatalogBookDetailResponse2 created = catalogService.importFromGoogleBooks(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * API 4: Thêm mới tài liệu trực tiếp vào kho sách (Áp dụng Factory Method Pattern)
     * Hỗ trợ EBOOK, AUDIOBOOK, PHYSICAL với kiểm tra tính hợp lệ và LSP/ISP.
     * URL: POST /api/books/v2/factory/create
     */
    @PostMapping("/factory/create")
    public ResponseEntity<?> createBookViaFactory(@RequestBody CatalogItemRequest2 request) {
        try {
            CatalogBookDetailResponse2 created = catalogService.createBookViaFactory(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * API 5: Lấy thông tin chi tiết tài liệu kèm metadata và hướng dẫn sử dụng chuyên biệt
     * URL: GET /api/books/v2/{bookId}/metadata
     */
    @GetMapping("/{bookId}/metadata")
    public ResponseEntity<?> getBookMetadata(@PathVariable Long bookId) {
        try {
            CatalogBookDetailResponse2 detail = catalogService.getBookDetail(bookId);
            return ResponseEntity.ok(detail);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
