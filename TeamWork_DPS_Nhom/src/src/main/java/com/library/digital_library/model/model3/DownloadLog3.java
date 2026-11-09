package com.library.digital_library.model.model3;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Lịch sử kiểm toán các lần truy cập tải E-Book được kiểm soát qua Protection Proxy (Thành viên 3)
 */
@Entity
@Table(name = "download_logs_3")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DownloadLog3 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private Long bookId;

    private String licenseKey;

    @Column(nullable = false)
    private LocalDateTime requestedAt;

    @Column(nullable = false)
    private String status; // "SUCCESS" hoặc "DENIED"

    private String reason; // Lý do từ chối hoặc mô tả thành công

    private String clientIp;
}
