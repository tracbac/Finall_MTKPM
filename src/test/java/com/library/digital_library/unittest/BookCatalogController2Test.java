package com.library.digital_library.unittest;

import com.library.digital_library.controller.controller2.BookCatalogController2;
import com.library.digital_library.model.model2.CatalogBookDetailResponse2;
import com.library.digital_library.model.model2.CatalogItemRequest2;
import com.library.digital_library.model.model2.GoogleImportRequest2;
import com.library.digital_library.service.service2.BookCatalogService2;
import com.library.digital_library.service.service2.adapter.ExternalBookDto2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Test cho BookCatalogController2 REST API (Thành viên 2).
 * Sử dụng Test Double (Subclassing) để tương thích 100% với môi trường JVM hiện đại.
 */
@DisplayName("Kiểm thử BookCatalogController2 REST API (Thành viên 2)")
public class BookCatalogController2Test {

    private CatalogBookDetailResponse2 sampleDetail;
    private ExternalBookDto2 sampleGoogleBook;
    private boolean shouldThrowNotFound = false;
    private boolean shouldThrowBadRequest = false;

    private BookCatalogController2 controller;

    @BeforeEach
    void setUp() {
        shouldThrowNotFound = false;
        shouldThrowBadRequest = false;

        sampleDetail = CatalogBookDetailResponse2.builder()
                .id(1L)
                .title("Design Patterns")
                .author("GoF")
                .itemType("EBOOK")
                .rentalPrice(BigDecimal.valueOf(10000))
                .coverPrice(BigDecimal.valueOf(100000))
                .fileFormat("PDF")
                .build();

        sampleGoogleBook = ExternalBookDto2.builder()
                .title("Clean Architecture")
                .author("Uncle Bob")
                .build();

        // Anonymous Subclass thay thế Mockito để tránh giới hạn Java 25 class modification
        BookCatalogService2 stubService = new BookCatalogService2(null, null, null, null, null, null) {
            @Override
            public List<CatalogBookDetailResponse2> searchCatalog(String keyword, String author, String category,
                                                                 Boolean onlyAvailable, String strategyKey) {
                return List.of(sampleDetail);
            }

            @Override
            public List<ExternalBookDto2> previewFromGoogleBooks(String query, int maxResults) {
                return List.of(sampleGoogleBook);
            }

            @Override
            public CatalogBookDetailResponse2 importFromGoogleBooks(GoogleImportRequest2 request) {
                if (shouldThrowBadRequest || request.getQuery() == null && request.getIsbn() == null) {
                    throw new IllegalArgumentException("Cần cung cấp ít nhất từ khóa");
                }
                return sampleDetail;
            }

            @Override
            public CatalogBookDetailResponse2 createBookViaFactory(CatalogItemRequest2 request) {
                return sampleDetail;
            }

            @Override
            public CatalogBookDetailResponse2 getBookDetail(Long bookId) {
                if (shouldThrowNotFound || bookId == 999L) {
                    throw new NoSuchElementException("Không tìm thấy sách");
                }
                return sampleDetail;
            }
        };

        controller = new BookCatalogController2(stubService);
    }

    @Test
    @DisplayName("GET /api/books/v2/search - Tìm kiếm thành công trả về HTTP 200")
    void testSearchCatalog() {
        ResponseEntity<List<CatalogBookDetailResponse2>> response =
                controller.searchCatalog("Design", null, null, false, "TITLE");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Design Patterns", response.getBody().get(0).getTitle());
    }

    @Test
    @DisplayName("GET /api/books/v2/google/search - Xem trước sách từ Google Books trả về HTTP 200")
    void testSearchGoogleBooks() {
        ResponseEntity<List<ExternalBookDto2>> response = controller.searchGoogleBooks("Clean", 5);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Clean Architecture", response.getBody().get(0).getTitle());
    }

    @Test
    @DisplayName("POST /api/books/v2/google/import - Import thành công trả về HTTP 201 CREATED")
    void testImportFromGoogleBooks_Success() {
        GoogleImportRequest2 req = GoogleImportRequest2.builder().query("Clean").itemType("EBOOK").build();
        ResponseEntity<?> response = controller.importFromGoogleBooks(req);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(sampleDetail, response.getBody());
    }

    @Test
    @DisplayName("POST /api/books/v2/google/import - Thiếu thông tin trả về HTTP 400 BAD REQUEST")
    void testImportFromGoogleBooks_BadRequest() {
        GoogleImportRequest2 req = new GoogleImportRequest2();
        ResponseEntity<?> response = controller.importFromGoogleBooks(req);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Cần cung cấp ít nhất từ khóa", response.getBody());
    }

    @Test
    @DisplayName("POST /api/books/v2/factory/create - Tạo sách thành công trả về HTTP 201 CREATED")
    void testCreateBookViaFactory_Success() {
        CatalogItemRequest2 req = CatalogItemRequest2.builder().title("Book").author("Author").itemType("PHYSICAL").build();
        ResponseEntity<?> response = controller.createBookViaFactory(req);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    @DisplayName("GET /api/books/v2/{id}/metadata - Sách tồn tại trả về HTTP 200")
    void testGetBookMetadata_Success() {
        ResponseEntity<?> response = controller.getBookMetadata(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(sampleDetail, response.getBody());
    }

    @Test
    @DisplayName("GET /api/books/v2/{id}/metadata - Sách không tồn tại trả về HTTP 404 NOT FOUND")
    void testGetBookMetadata_NotFound() {
        ResponseEntity<?> response = controller.getBookMetadata(999L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Không tìm thấy sách", response.getBody());
    }
}
