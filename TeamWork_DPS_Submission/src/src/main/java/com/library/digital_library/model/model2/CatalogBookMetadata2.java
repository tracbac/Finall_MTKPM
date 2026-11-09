package com.library.digital_library.model.model2;

import com.library.digital_library.model.Book;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity lưu trữ metadata mở rộng cho từng loại tài liệu trong kho sách (Thành viên 2).
 * Tách biệt hoàn toàn để không làm biến đổi cấu trúc bảng books cốt lõi.
 */
@Entity
@Table(name = "catalog_book_metadata_2")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CatalogBookMetadata2 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false, unique = true)
    private Book book;

    @Column(name = "isbn", length = 50)
    private String isbn;

    @Column(name = "item_type", nullable = false, length = 30)
    private String itemType; // EBOOK, AUDIOBOOK, PHYSICAL

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "google_volume_id", length = 100)
    private String googleVolumeId;

    // --- Thuộc tính riêng cho E-Book (DigitalDownloadable2) ---
    @Column(name = "file_format", length = 20)
    private String fileFormat; // PDF, EPUB, MOBI

    @Column(name = "file_size_mb")
    private Double fileSizeMb;

    @Column(name = "download_url", length = 1000)
    private String downloadUrl;

    // --- Thuộc tính riêng cho Audio Book (AudioStreamable2) ---
    @Column(name = "audio_duration_minutes")
    private Integer audioDurationMinutes;

    @Column(name = "narrator", length = 255)
    private String narrator;

    @Column(name = "stream_url", length = 1000)
    private String streamUrl;

    // --- Thuộc tính riêng cho Sách giấy (PhysicalShelvable2) ---
    @Column(name = "shelf_location", length = 100)
    private String shelfLocation; // Ví dụ: Kệ A-12, Tầng 2
}

