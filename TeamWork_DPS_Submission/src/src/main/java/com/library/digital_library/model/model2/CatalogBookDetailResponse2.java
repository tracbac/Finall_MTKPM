package com.library.digital_library.model.model2;

import com.library.digital_library.model.Book;
import com.library.digital_library.model.BookPrice;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO trả về chi tiết toàn diện của tài liệu trong kho sách (Thành viên 2).
 * Tích hợp dữ liệu từ Book, BookPrice và CatalogBookMetadata2.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CatalogBookDetailResponse2 {

    private Long id;
    private String title;
    private String author;
    private String category;
    private Integer quantity;
    private String imageUrl;

    private BigDecimal rentalPrice;
    private BigDecimal coverPrice;

    private String isbn;
    private String itemType;
    private String description;
    private String googleVolumeId;
    private String usageInstruction;

    // Chi tiết E-Book (DigitalDownloadable2)
    private String fileFormat;
    private Double fileSizeMb;
    private String downloadUrl;

    // Chi tiết Audio Book (AudioStreamable2)
    private Integer audioDurationMinutes;
    private String narrator;
    private String streamUrl;

    // Chi tiết Physical Book (PhysicalShelvable2)
    private String shelfLocation;
    private Boolean availableForPhysicalLoan;

    public static CatalogBookDetailResponse2 from(Book book, BookPrice price, CatalogBookMetadata2 metadata, String usageInstruction) {
        CatalogBookDetailResponse2Builder builder = CatalogBookDetailResponse2.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .category(book.getCategory())
                .quantity(book.getQuantity())
                .imageUrl(book.getImageUrl())
                .rentalPrice(price != null ? price.getRentalPrice() : BigDecimal.ZERO)
                .coverPrice(price != null ? price.getCoverPrice() : BigDecimal.ZERO)
                .usageInstruction(usageInstruction);

        if (metadata != null) {
            builder.isbn(metadata.getIsbn())
                    .itemType(metadata.getItemType())
                    .description(metadata.getDescription())
                    .googleVolumeId(metadata.getGoogleVolumeId())
                    .fileFormat(metadata.getFileFormat())
                    .fileSizeMb(metadata.getFileSizeMb())
                    .downloadUrl(metadata.getDownloadUrl())
                    .audioDurationMinutes(metadata.getAudioDurationMinutes())
                    .narrator(metadata.getNarrator())
                    .streamUrl(metadata.getStreamUrl())
                    .shelfLocation(metadata.getShelfLocation())
                    .availableForPhysicalLoan("PHYSICAL".equalsIgnoreCase(metadata.getItemType())
                            && book.getQuantity() != null && book.getQuantity() > 0);
        } else {
            builder.itemType("PHYSICAL")
                    .availableForPhysicalLoan(book.getQuantity() != null && book.getQuantity() > 0);
        }

        return builder.build();
    }
}

