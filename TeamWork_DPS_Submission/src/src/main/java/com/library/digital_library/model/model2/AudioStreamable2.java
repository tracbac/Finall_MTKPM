package com.library.digital_library.model.model2;

/**
 * Interface Segregation Principle (ISP) - Giao diện chuyên biệt cho sách nói (Audio Book).
 * Chỉ các tài liệu có âm thanh và phát trực tuyến mới cài đặt giao diện này.
 */
public interface AudioStreamable2 {
    Integer getDurationMinutes();
    String getNarrator();
    String getStreamUrl();
}

