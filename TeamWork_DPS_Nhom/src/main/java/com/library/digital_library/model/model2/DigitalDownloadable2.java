package com.library.digital_library.model.model2;

/**
 * Interface Segregation Principle (ISP) - Giao diện chuyên biệt cho tài liệu điện tử có thể tải về.
 * Các loại tài liệu không có tính năng tải về (như sách giấy PhysicalBook) không bị ép phải phụ thuộc vào giao diện này.
 */
public interface DigitalDownloadable2 {
    String getFileFormat();
    Double getFileSizeMb();
    String getDownloadUrl();
}

