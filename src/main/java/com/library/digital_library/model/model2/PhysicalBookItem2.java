package com.library.digital_library.model.model2;

import com.library.digital_library.model.Book;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Concrete Product 3: Sách giấy truyền thống (Physical Book).
 * Tuân thủ ISP: Chỉ cài đặt PhysicalShelvable2, không bị ép cài các hàm tải file hay streaming audio.
 * Tuân thủ LSP: Thay thế hoàn hảo cho LibraryItem2.
 */
@Getter
@Setter
public class PhysicalBookItem2 extends LibraryItem2 implements PhysicalShelvable2 {

    private String shelfLocation;

    public PhysicalBookItem2() {
        super();
        this.shelfLocation = "Kho trung tâm - Kệ tổng";
    }

    public PhysicalBookItem2(String title, String author, String category, String isbn,
                             Integer quantity, String imageUrl, String description,
                             BigDecimal coverPrice, BigDecimal rentalPrice,
                             String shelfLocation) {
        super(title, author, category, isbn, quantity, imageUrl, description, coverPrice, rentalPrice);
        this.shelfLocation = (shelfLocation != null && !shelfLocation.isBlank()) ? shelfLocation : "Kho trung tâm - Kệ tổng";
    }

    @Override
    public String getItemType() {
        return "PHYSICAL";
    }

    @Override
    public String getShelfLocation() {
        return this.shelfLocation;
    }

    @Override
    public boolean isAvailableForPhysicalLoan() {
        return this.quantity != null && this.quantity > 0;
    }

    @Override
    public String getUsageInstruction() {
        return "Sách in truyền thống đặt tại: [" + shelfLocation + "]. "
                + (isAvailableForPhysicalLoan()
                ? "Hiện đang có sẵn (" + quantity + " bản) để mượn tại quầy thủ thư."
                : "Tạm thời hết sách trên giá, vui lòng đặt trước.");
    }

    @Override
    public CatalogBookMetadata2 toMetadataEntity(Book savedBook) {
        return CatalogBookMetadata2.builder()
                .book(savedBook)
                .isbn(this.isbn)
                .itemType(getItemType())
                .description(this.description)
                .shelfLocation(this.shelfLocation)
                .build();
    }
}

