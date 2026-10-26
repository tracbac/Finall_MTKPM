package com.library.digital_library.model.model3;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Kết quả tải E-Book trả về khi Protection Proxy cho phép truy cập
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DownloadResult3 {
    private boolean success;
    private String fileName;
    private String fileFormat;       // PDF, EPUB
    private Long fileSize;           // Kích thước byte
    private String downloadUrl;      // Đường dẫn tải bảo mật hoặc token stream
    private String drmWatermark;     // Thủy vân bản quyền số
    private Integer remainingDownloads; // Số lần tải còn lại
    private String message;
}
