package com.library.digital_library.model.model2;

import com.library.digital_library.model.Book;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Lớp cơ sở trừu tượng cho mọi tài liệu trong thư viện (Factory Method Product Base).
 * Tuân thủ Liskov Substitution Principle (LSP):
 * - Chỉ định nghĩa các thuộc tính và hành vi chung mà mọi loại sách đều hỗ trợ hợp lệ.
 * - Không có phương thức nào ném UnsupportedOperationException. Mọi lớp con (EBook, AudioBook, PhysicalBook)
 *   đều có thể thay thế hoàn hảo cho LibraryItem2 ở mọi nơi trong hệ thống.
 */
@Getter
@Setter
public abstract class LibraryItem2 {

    protected String title;
    protected String author;
    protected String category;
    protected String isbn;
    protected Integer quantity;
    protected String imageUrl;
    protected String description;
    protected BigDecimal coverPrice;
    protected BigDecimal rentalPrice;

    public LibraryItem2() {
    }

    public LibraryItem2(String title, String author, String category, String isbn,
                        Integer quantity, String imageUrl, String description,
                        BigDecimal coverPrice, BigDecimal rentalPrice) {
        this.title = title;
        this.author = author;
        this.category = category;
        this.isbn = isbn;
        this.quantity = quantity != null ? quantity : 0;
        this.imageUrl = imageUrl;
        this.description = description;
        this.coverPrice = coverPrice != null ? coverPrice : BigDecimal.ZERO;
        this.rentalPrice = rentalPrice != null ? rentalPrice : BigDecimal.ZERO;
    }

    /**
     * Định danh loại tài liệu (EBOOK, AUDIOBOOK, PHYSICAL)
     */
    public abstract String getItemType();

    /**
     * Hướng dẫn sử dụng/tiếp cận tài liệu theo từng phương thức cụ thể.
     */
    public abstract String getUsageInstruction();

    /**
     * Chuyển đổi dữ liệu cốt lõi sang Entity Book của hệ thống gốc.
     */
    public Book toBookEntity() {
        Book book = new Book();
        book.setTitle(this.title != null ? this.title.trim() : "");
        book.setAuthor(this.author != null ? this.author.trim() : "");
        book.setCategory(this.category != null ? this.category.trim() : null);
        book.setQuantity(this.quantity != null ? this.quantity : 0);
        book.setImageUrl(this.imageUrl);
        return book;
    }

    /**
     * Khởi tạo Entity CatalogBookMetadata2 liên kết với Book đã lưu.
     */
    public abstract CatalogBookMetadata2 toMetadataEntity(Book savedBook);
}

