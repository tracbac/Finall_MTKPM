package com.library.digital_library.model.model2;

import com.library.digital_library.model.Book;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Concrete Product 1: Sách điện tử (E-Book).
 * Tuân thủ ISP: Chỉ cài đặt DigitalDownloadable2, không bị ép cài các hàm âm thanh hay vị trí kệ.
 * Tuân thủ LSP: Thay thế hoàn hảo cho LibraryItem2.
 */
@Getter
@Setter
public class EBookItem2 extends LibraryItem2 implements DigitalDownloadable2 {

    private String fileFormat; // PDF, EPUB, MOBI
    private Double fileSizeMb;
    private String downloadUrl;

    public EBookItem2() {
        super();
        this.fileFormat = "PDF";
    }

    public EBookItem2(String title, String author, String category, String isbn,
                      Integer quantity, String imageUrl, String description,
                      BigDecimal coverPrice, BigDecimal rentalPrice,
                      String fileFormat, Double fileSizeMb, String downloadUrl) {
        super(title, author, category, isbn, quantity, imageUrl, description, coverPrice, rentalPrice);
        this.fileFormat = (fileFormat != null && !fileFormat.isBlank()) ? fileFormat.toUpperCase() : "PDF";
        this.fileSizeMb = fileSizeMb != null ? fileSizeMb : 5.0;
        this.downloadUrl = downloadUrl;
    }

    @Override
    public String getItemType() {
        return "EBOOK";
    }

    @Override
    public String getUsageInstruction() {
        return "Tài liệu điện tử định dạng " + fileFormat + " (Dung lượng: " + fileSizeMb + "MB). "
                + "Bạn có thể đọc trực tuyến qua trình duyệt hoặc tải về thiết bị cá nhân.";
    }

    @Override
    public CatalogBookMetadata2 toMetadataEntity(Book savedBook) {
        return CatalogBookMetadata2.builder()
                .book(savedBook)
                .isbn(this.isbn)
                .itemType(getItemType())
                .description(this.description)
                .fileFormat(this.fileFormat)
                .fileSizeMb(this.fileSizeMb)
                .downloadUrl(this.downloadUrl)
                .build();
    }
}

