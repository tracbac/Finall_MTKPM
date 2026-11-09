package com.library.digital_library.model.model3;

import com.library.digital_library.model.Book;
import com.library.digital_library.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Cấp phép bản quyền số (Digital Rights Management - DRM License) cho E-Book (Thành viên 3)
 */
@Entity
@Table(name = "drm_licenses_3")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DrmLicense3 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String licenseKey; // Mã cấp phép bản quyền số duy nhất (UUID)

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(nullable = false)
    private LocalDate issueDate; // Ngày cấp phép

    @Column(nullable = false)
    private LocalDate expiryDate; // Ngày hết hạn bản quyền

    @Builder.Default
    private Integer downloadLimit = 3; // Số lượt tải tối đa cho phép

    @Builder.Default
    private Integer downloadCount = 0; // Số lượt đã tải thực tế

    @Builder.Default
    private String status = "ACTIVE"; // ACTIVE, EXPIRED, REVOKED

    private String watermark; // Thông tin nhúng thủy vân bản quyền: "User: dangt - License: ..."
}
