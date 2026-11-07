package com.library.digital_library.service.service2.adapter;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
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

        // 2. Chuẩn hóa thể loại sách từ Google Books sang thể loại giao diện
        String categoryStr = normalizeCategory(info.getCategories(), info.getTitle());

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

        // 4. Lấy URL ảnh bìa (bảo đảm luôn dùng HTTPS)
        String imageUrl = null;
        if (info.getImageLinks() != null) {
            imageUrl = info.getImageLinks().getThumbnail() != null
                    ? info.getImageLinks().getThumbnail()
                    : info.getImageLinks().getSmallThumbnail();
            if (imageUrl != null && imageUrl.startsWith("http://")) {
                imageUrl = imageUrl.replace("http://", "https://");
            }
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

    /**
     * Chuẩn hóa danh mục sách từ Google Books sang danh mục tiêu chuẩn của hệ thống.
     */
    private String normalizeCategory(List<String> rawCategories, String title) {
        if (rawCategories != null && !rawCategories.isEmpty()) {
            String combined = String.join(" ", rawCategories).toLowerCase(Locale.ROOT);
            if (combined.contains("medical") || combined.contains("health") || combined.contains("medicine")
                    || combined.contains("nursing") || combined.contains("clinical") || combined.contains("wellness")) {
                if (combined.contains("psychology") || combined.contains("mental")) {
                    return "Y tế & Tâm lý";
                }
                return "Y tế & Sức khỏe";
            }
            if (combined.contains("psychology") || combined.contains("mental") || combined.contains("behavioral")) {
                return "Tâm lý học";
            }
            if (combined.contains("computer") || combined.contains("software") || combined.contains("programming")
                    || combined.contains("technology") || combined.contains("data") || combined.contains("algorithm")) {
                return "Công nghệ thông tin";
            }
            if (combined.contains("business") || combined.contains("economic") || combined.contains("finance")
                    || combined.contains("management") || combined.contains("marketing") || combined.contains("commerce")) {
                return "Kinh tế & Quản trị";
            }
            if (combined.contains("history") || combined.contains("archaeology") || combined.contains("civilization")) {
                return "Lịch sử & Xã hội";
            }
            if (combined.contains("science") || combined.contains("mathematics") || combined.contains("physics")
                    || combined.contains("chemistry") || combined.contains("biology")) {
                return "Khoa học tự nhiên";
            }
            if (combined.contains("fiction") || combined.contains("literature") || combined.contains("art")
                    || combined.contains("poetry") || combined.contains("drama") || combined.contains("novel")) {
                return "Văn học & Nghệ thuật";
            }
            if (combined.contains("education") || combined.contains("teaching") || combined.contains("learning")) {
                return "Giáo dục & Kỹ năng";
            }
            return rawCategories.get(0);
        }

        // Suy luận danh mục từ tiêu đề sách nếu Google Books không gắn thẻ category
        if (title != null) {
            String lowerTitle = title.toLowerCase(Locale.ROOT);
            if (lowerTitle.contains("health") || lowerTitle.contains("medical") || lowerTitle.contains("sức khỏe")
                    || lowerTitle.contains("y tế") || lowerTitle.contains("bệnh") || lowerTitle.contains("thuốc")) {
                return "Y tế & Sức khỏe";
            }
            if (lowerTitle.contains("java") || lowerTitle.contains("python") || lowerTitle.contains("code")
                    || lowerTitle.contains("software") || lowerTitle.contains("architecture") || lowerTitle.contains("pattern")) {
                return "Công nghệ thông tin";
            }
            if (lowerTitle.contains("psychology") || lowerTitle.contains("tâm lý")) {
                return "Tâm lý học";
            }
            if (lowerTitle.contains("business") || lowerTitle.contains("finance") || lowerTitle.contains("kinh tế")
                    || lowerTitle.contains("quản trị") || lowerTitle.contains("money")) {
                return "Kinh tế & Quản trị";
            }
        }
        return "Tổng hợp";
    }
}

