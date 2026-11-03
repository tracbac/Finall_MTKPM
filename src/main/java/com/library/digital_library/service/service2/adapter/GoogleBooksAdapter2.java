package com.library.digital_library.service.service2.adapter;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Adapter trong Adapter Pattern (Object Adapter).
 * Áp dụng Composition (Kết hợp) và Delegation (Ủy quyền):
 * - Giữ tham chiếu đến GoogleBooksRestClient2 (Adaptee).
 * - Cài đặt ExternalBookCatalogPort2 (Target Interface).
 * - Chuyển đổi dữ liệu từ cấu trúc phức tạp của Google Books API thành DTO chuẩn của hệ thống nội bộ.
 */
@Component
public class GoogleBooksAdapter2 implements ExternalBookCatalogPort2 {

    private final GoogleBooksRestClient2 googleClient;

    public GoogleBooksAdapter2(GoogleBooksRestClient2 googleClient) {
        this.googleClient = googleClient;
    }

    @Override
    public List<ExternalBookDto2> searchBooks(String query, int maxResults) {
        GoogleBooksModel2.GoogleVolumesResponse2 response = googleClient.queryGoogleVolumesApi(query, maxResults);
        if (response == null || response.getItems() == null || response.getItems().isEmpty()) {
            return Collections.emptyList();
        }

        return response.getItems().stream()
                .map(this::adaptToExternalBookDto)
                .toList();
    }

    @Override
    public List<ExternalBookDto2> searchBooks(String query, int page, int maxResults) {
        if (page <= 1) {
            return searchBooks(query, maxResults);
        }
        GoogleBooksModel2.GoogleVolumesResponse2 response = googleClient.queryGoogleVolumesApi(query, page, maxResults);
        if (response == null || response.getItems() == null || response.getItems().isEmpty()) {
            return Collections.emptyList();
        }

        return response.getItems().stream()
                .map(this::adaptToExternalBookDto)
                .toList();
    }

    @Override
    public Optional<ExternalBookDto2> findBookByIsbn(String isbn) {
        if (isbn == null || isbn.isBlank()) {
            return Optional.empty();
        }
        String cleanIsbn = isbn.trim().replace("-", "");
        GoogleBooksModel2.GoogleVolumesResponse2 response = googleClient.queryGoogleVolumesApi("isbn:" + cleanIsbn, 1);

        if (response == null || response.getItems() == null || response.getItems().isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(adaptToExternalBookDto(response.getItems().get(0)));
    }

    /**
     * Phương thức lõi thực hiện Adapt (Chuyển đổi các thuộc tính bất tương thích).
     */
    private ExternalBookDto2 adaptToExternalBookDto(GoogleBooksModel2.GoogleVolumeItem2 item) {
        GoogleBooksModel2.VolumeInfo info = item.getVolumeInfo() != null
                ? item.getVolumeInfo()
                : new GoogleBooksModel2.VolumeInfo();

        // 1. Chuyển mảng tác giả thành chuỗi phân tách bằng dấu phẩy
        String authorStr = (info.getAuthors() != null && !info.getAuthors().isEmpty())
                ? String.join(", ", info.getAuthors())
                : "Nhiều tác giả";

        // 2. Chuyển mảng thể loại thành chuỗi
        String categoryStr = (info.getCategories() != null && !info.getCategories().isEmpty())
                ? String.join(", ", info.getCategories())
                : "Chưa phân loại";

        // 3. Trích xuất mã ISBN (ưu tiên ISBN_13, sau đó ISBN_10)
        String extractedIsbn = null;
        if (info.getIndustryIdentifiers() != null) {
            for (GoogleBooksModel2.IndustryIdentifier id : info.getIndustryIdentifiers()) {
                if ("ISBN_13".equalsIgnoreCase(id.getType())) {
                    extractedIsbn = id.getIdentifier();
                    break;
                }
            }
            if (extractedIsbn == null) {
                for (GoogleBooksModel2.IndustryIdentifier id : info.getIndustryIdentifiers()) {
                    if ("ISBN_10".equalsIgnoreCase(id.getType())) {
                        extractedIsbn = id.getIdentifier();
                        break;
                    }
                }
            }
        }
        if (extractedIsbn == null) {
            extractedIsbn = "ISBN-" + item.getId();
        }

        // 4. Lấy URL ảnh bìa
        String imageUrl = null;
        if (info.getImageLinks() != null) {
            imageUrl = info.getImageLinks().getThumbnail() != null
                    ? info.getImageLinks().getThumbnail()
                    : info.getImageLinks().getSmallThumbnail();
        }

        // 5. Tính toán giá bìa và giá thuê ước tính dựa trên số trang (pageCount)
        int pages = info.getPageCount() != null && info.getPageCount() > 0 ? info.getPageCount() : 300;
        BigDecimal estimatedCoverPrice = BigDecimal.valueOf(Math.max(pages * 450L, 60000L))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal estimatedRentalPrice = estimatedCoverPrice.multiply(BigDecimal.valueOf(0.15))
                .setScale(2, RoundingMode.HALF_UP);

        return ExternalBookDto2.builder()
                .googleVolumeId(item.getId())
                .title(info.getTitle() != null ? info.getTitle() : "Tài liệu không tên")
                .author(authorStr)
                .category(categoryStr)
                .isbn(extractedIsbn)
                .description(info.getDescription())
                .imageUrl(imageUrl)
                .pageCount(pages)
                .estimatedCoverPrice(estimatedCoverPrice)
                .estimatedRentalPrice(estimatedRentalPrice)
                .previewLink(info.getPreviewLink())
                .publisher(info.getPublisher())
                .publishedDate(info.getPublishedDate())
                .build();
    }
}

