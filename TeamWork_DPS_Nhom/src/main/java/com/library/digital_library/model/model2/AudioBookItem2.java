package com.library.digital_library.model.model2;

import com.library.digital_library.model.Book;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Concrete Product 2: Sách nói (Audio Book).
 * Tuân thủ ISP: Chỉ cài đặt AudioStreamable2, không bị ép cài các hàm tải PDF hay vị trí kệ.
 * Tuân thủ LSP: Thay thế hoàn hảo cho LibraryItem2.
 */
@Getter
@Setter
public class AudioBookItem2 extends LibraryItem2 implements AudioStreamable2 {

    private Integer durationMinutes;
    private String narrator;
    private String streamUrl;

    public AudioBookItem2() {
        super();
        this.durationMinutes = 60;
    }

    public AudioBookItem2(String title, String author, String category, String isbn,
                          Integer quantity, String imageUrl, String description,
                          BigDecimal coverPrice, BigDecimal rentalPrice,
                          Integer durationMinutes, String narrator, String streamUrl) {
        super(title, author, category, isbn, quantity, imageUrl, description, coverPrice, rentalPrice);
        this.durationMinutes = durationMinutes != null ? durationMinutes : 60;
        this.narrator = (narrator != null && !narrator.isBlank()) ? narrator : "Giọng đọc AI / Tác giả";
        this.streamUrl = streamUrl;
    }

    @Override
    public String getItemType() {
        return "AUDIOBOOK";
    }

    @Override
    public String getUsageInstruction() {
        return "Sách nói với thời lượng " + durationMinutes + " phút, thuyết minh bởi " + narrator + ". "
                + "Bạn có thể phát âm thanh trực tiếp (streaming) với trình phát tích hợp.";
    }

    @Override
    public CatalogBookMetadata2 toMetadataEntity(Book savedBook) {
        return CatalogBookMetadata2.builder()
                .book(savedBook)
                .isbn(this.isbn)
                .itemType(getItemType())
                .description(this.description)
                .audioDurationMinutes(this.durationMinutes)
                .narrator(this.narrator)
                .streamUrl(this.streamUrl)
                .build();
    }
}

