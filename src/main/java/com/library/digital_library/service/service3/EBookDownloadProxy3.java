package com.library.digital_library.service.service3;

import com.library.digital_library.model.User;
import com.library.digital_library.model.model3.*;
import com.library.digital_library.repository.UserRepository;
import com.library.digital_library.repository.repository3.DownloadLog3Repository;
import com.library.digital_library.repository.repository3.DrmLicense3Repository;
import com.library.digital_library.repository.repository3.FineReceipt3Repository;
import com.library.digital_library.repository.repository3.LoanRecord3Repository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * PROTECTION PROXY (Proxy Pattern - Thành viên 3)
 * Mô tả thực tế trong hệ thống: Kiểm soát quyền truy cập/tải tài liệu (Chỉ user có quyền mới được tải)
 * Đóng vai trò lớp bảo vệ (Gatekeeper / Protection Proxy) đứng trước RealEBookDownloadService3
 */
@Service("eBookDownloadProxy3")
@Primary
@Slf4j
public class EBookDownloadProxy3 implements EBookDownloadService3 {

    private final EBookDownloadService3 realService;
    private final DrmLicense3Repository drmLicenseRepository;
    private final FineReceipt3Repository fineReceiptRepository;
    private final LoanRecord3Repository loanRecordRepository;
    private final UserRepository userRepository;
    private final DownloadLog3Repository downloadLogRepository;

    public EBookDownloadProxy3(
            @Qualifier("realEBookDownloadService3") EBookDownloadService3 realService,
            DrmLicense3Repository drmLicenseRepository,
            FineReceipt3Repository fineReceiptRepository,
            LoanRecord3Repository loanRecordRepository,
            UserRepository userRepository,
            DownloadLog3Repository downloadLogRepository) {
        this.realService = realService;
        this.drmLicenseRepository = drmLicenseRepository;
        this.fineReceiptRepository = fineReceiptRepository;
        this.loanRecordRepository = loanRecordRepository;
        this.userRepository = userRepository;
        this.downloadLogRepository = downloadLogRepository;
    }

    @Override
    @Transactional
    public DownloadResult3 downloadEBook(DownloadRequest3 request) {
        log.info("[Proxy Pattern] Intercepting E-Book download request for User ID: {}, Book ID: {}",
                request.getUserId(), request.getBookId());

        // 1. Kiểm tra sự tồn tại của người dùng
        User user = userRepository.findById(request.getUserId()).orElse(null);
        if (user == null) {
            logAudit(request, "DENIED", "Người dùng không tồn tại trong hệ thống");
            throw new SecurityException("[Proxy từ chối] Không tìm thấy thông tin người dùng!");
        }

        // 2. Kiểm tra chặn truy cập do nợ phạt quá hạn chưa trả
        long unpaidFines = fineReceiptRepository.countByUserIdAndIsPaidFalse(user.getId());
        if (unpaidFines > 0) {
            logAudit(request, "DENIED", "Người dùng có " + unpaidFines + " khoản phạt quá hạn chưa thanh toán");
            throw new SecurityException("[Proxy từ chối] Bạn đang nợ phạt quá hạn mượn sách. Vui lòng thanh toán phạt trước khi tải tài liệu!");
        }

        // 3. Kiểm tra chặn truy cập do đang giữ sách quá hạn
        long overdueLoans = loanRecordRepository.countByUserIdAndStatus(user.getId(), LoanStatus3.OVERDUE);
        if (overdueLoans > 0) {
            logAudit(request, "DENIED", "Người dùng đang có " + overdueLoans + " sách mượn quá hạn chưa trả");
            throw new SecurityException("[Proxy từ chối] Bạn đang có sách mượn quá hạn chưa hoàn trả. Quyền tải tài liệu bị tạm khóa!");
        }

        // 4. Kiểm tra bản quyền số DRM (DRM License Authorization)
        if (request.getLicenseKey() == null || request.getLicenseKey().trim().isEmpty()) {
            logAudit(request, "DENIED", "Thiếu mã bản quyền số DRM");
            throw new SecurityException("[Proxy từ chối] Yêu cầu mã giấy phép bản quyền số DRM để tải tài liệu!");
        }

        DrmLicense3 license = drmLicenseRepository.findByLicenseKey(request.getLicenseKey().trim()).orElse(null);
        if (license == null) {
            logAudit(request, "DENIED", "Mã bản quyền số DRM không tồn tại");
            throw new SecurityException("[Proxy từ chối] Giấy phép bản quyền số không tồn tại hoặc không hợp lệ!");
        }

        // Kiểm tra license có thuộc về đúng user và đúng sách không
        if (!license.getUser().getId().equals(user.getId()) || !license.getBook().getId().equals(request.getBookId())) {
            logAudit(request, "DENIED", "Mã bản quyền không khớp với User hoặc Sách");
            throw new SecurityException("[Proxy từ chối] Giấy phép DRM này không cấp cho tài khoản hoặc sách được yêu cầu!");
        }

        // Kiểm tra trạng thái thu hồi (REVOKED)
        if ("REVOKED".equalsIgnoreCase(license.getStatus())) {
            logAudit(request, "DENIED", "Giấy phép bản quyền đã bị thu hồi");
            throw new SecurityException("[Proxy từ chối] Giấy phép bản quyền số này đã bị thu hồi!");
        }

        LocalDate today = LocalDate.now();
        // Kiểm tra hết hạn (EXPIRED)
        if (today.isAfter(license.getExpiryDate()) || "EXPIRED".equalsIgnoreCase(license.getStatus())) {
            license.setStatus("EXPIRED");
            drmLicenseRepository.save(license);
            logAudit(request, "DENIED", "Giấy phép bản quyền đã hết hạn vào ngày " + license.getExpiryDate());
            throw new SecurityException("[Proxy từ chối] Giấy phép bản quyền số đã hết hạn! Vui lòng gia hạn hoặc xin cấp phép mới.");
        }

        // Kiểm tra hạn mức số lượt tải (Download Limit)
        if (license.getDownloadCount() >= license.getDownloadLimit()) {
            logAudit(request, "DENIED", "Đã đạt giới hạn số lần tải tối đa: " + license.getDownloadLimit());
            throw new SecurityException("[Proxy từ chối] Bạn đã tải hết số lượt tối đa cho phép (" + license.getDownloadLimit() + " lần)!");
        }

        // 5. Mọi kiểm tra hợp lệ -> Cập nhật số lần tải
        license.setDownloadCount(license.getDownloadCount() + 1);
        drmLicenseRepository.save(license);

        // 6. Proxy ủy quyền cho Real Subject thực hiện tải thật sự
        log.info("[Proxy Pattern] All access controls passed. Delegating to RealEBookDownloadService...");
        DownloadResult3 result = realService.downloadEBook(request);

        // Đính kèm siêu dữ liệu DRM Watermark và lượt tải còn lại
        int remaining = license.getDownloadLimit() - license.getDownloadCount();
        result.setDrmWatermark(license.getWatermark());
        result.setRemainingDownloads(remaining);

        // 7. Ghi nhận kiểm toán thành công (Audit Log)
        logAudit(request, "SUCCESS", "Được phép tải. Lượt tải còn lại: " + remaining);

        return result;
    }

    private void logAudit(DownloadRequest3 request, String status, String reason) {
        try {
            DownloadLog3 auditLog = DownloadLog3.builder()
                    .userId(request.getUserId())
                    .bookId(request.getBookId())
                    .licenseKey(request.getLicenseKey())
                    .requestedAt(LocalDateTime.now())
                    .status(status)
                    .reason(reason)
                    .clientIp(request.getClientIp() != null ? request.getClientIp() : "127.0.0.1")
                    .build();
            downloadLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Lỗi ghi audit log trong Proxy: ", e);
        }
    }
}
