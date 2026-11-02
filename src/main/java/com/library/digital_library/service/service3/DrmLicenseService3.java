package com.library.digital_library.service.service3;

import com.library.digital_library.model.Book;
import com.library.digital_library.model.User;
import com.library.digital_library.model.model3.DrmLicense3;
import com.library.digital_library.model.model3.IssueDrmRequest3;
import com.library.digital_library.repository.BookRepository;
import com.library.digital_library.repository.UserRepository;
import com.library.digital_library.repository.repository3.DrmLicense3Repository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Service cấp phép & quản lý bản quyền số E-Book (Digital Rights Management - DRM Slice - Thành viên 3)
 */
@Service
@RequiredArgsConstructor
public class DrmLicenseService3 {

    private final DrmLicense3Repository drmLicenseRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    public static final int DEFAULT_VALID_DAYS = 30; // Mặc định bản quyền số có hiệu lực 30 ngày
    public static final int DEFAULT_DOWNLOAD_LIMIT = 3; // Mặc định tối đa 3 lượt tải

    /**
     * Cấp phép bản quyền số mới cho người dùng
     */
    @Transactional
    public DrmLicense3 issueLicense(IssueDrmRequest3 request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng ID: " + request.getUserId()));

        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sách ID: " + request.getBookId()));

        int validDays = (request.getValidDays() != null && request.getValidDays() > 0)
                ? request.getValidDays() : DEFAULT_VALID_DAYS;
        int downloadLimit = (request.getMaxDownloads() != null && request.getMaxDownloads() > 0)
                ? request.getMaxDownloads() : DEFAULT_DOWNLOAD_LIMIT;

        LocalDate today = LocalDate.now();
        String licenseKey = "DRM-" + UUID.randomUUID().toString().toUpperCase();

        String watermark = String.format("Bản quyền số được cấp cho [%s - Email: %s] | Tác phẩm: [%s] | Giấy phép: %s",
                user.getUsername(), user.getEmail(), book.getTitle(), licenseKey);

        DrmLicense3 license = DrmLicense3.builder()
                .licenseKey(licenseKey)
                .user(user)
                .book(book)
                .issueDate(today)
                .expiryDate(today.plusDays(validDays))
                .downloadLimit(downloadLimit)
                .downloadCount(0)
                .status("ACTIVE")
                .watermark(watermark)
                .build();

        return drmLicenseRepository.save(license);
    }

    /**
     * Thu hồi giấy phép bản quyền số
     */
    @Transactional
    public DrmLicense3 revokeLicense(String licenseKey, String reason) {
        DrmLicense3 license = drmLicenseRepository.findByLicenseKey(licenseKey)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy giấy phép DRM: " + licenseKey));

        license.setStatus("REVOKED");
        return drmLicenseRepository.save(license);
    }

    public List<DrmLicense3> getUserLicenses(Long userId) {
        return drmLicenseRepository.findByUserId(userId);
    }

    public DrmLicense3 getLicenseByKey(String licenseKey) {
        return drmLicenseRepository.findByLicenseKey(licenseKey)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy giấy phép DRM: " + licenseKey));
    }
}
