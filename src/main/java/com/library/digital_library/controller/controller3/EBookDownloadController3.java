package com.library.digital_library.controller.controller3;

import com.library.digital_library.model.model3.DownloadLog3;
import com.library.digital_library.model.model3.DownloadRequest3;
import com.library.digital_library.model.model3.DownloadResult3;
import com.library.digital_library.repository.repository3.DownloadLog3Repository;
import com.library.digital_library.service.service3.EBookDownloadService3;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller Tải E-Book có kiểm soát quyền truy cập qua Proxy Pattern (Thành viên 3)
 */
@RestController
@RequestMapping("/api/v3/ebooks")
@RequiredArgsConstructor
public class EBookDownloadController3 {

    // Tiêm EBookDownloadService3 - Spring sẽ tự động inject EBookDownloadProxy3 (được đánh dấu @Primary)
    private final EBookDownloadService3 eBookDownloadService;
    private final DownloadLog3Repository downloadLogRepository;

    /**
     * API Tải E-Book - Được kiểm soát hoàn toàn bởi Protection Proxy
     * - Chỉ người dùng có bản quyền hợp lệ, không nợ phạt mới được tải
     * - Proxy sẽ chặn và ném SecurityException nếu không đủ thẩm quyền (Trả về HTTP 403 FORBIDDEN)
     */
    @PostMapping("/download")
    public ResponseEntity<?> downloadEBook(@RequestBody DownloadRequest3 request, HttpServletRequest httpRequest) {
        try {
            if (request.getClientIp() == null || request.getClientIp().isEmpty()) {
                request.setClientIp(httpRequest.getRemoteAddr());
            }

            DownloadResult3 result = eBookDownloadService.downloadEBook(request);
            return ResponseEntity.ok(result);

        } catch (SecurityException e) {
            // Khi Proxy từ chối truy cập do vi phạm quyền (nợ phạt, hết hạn, quá lượt...)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(DownloadResult3.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build());

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(DownloadResult3.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(DownloadResult3.builder()
                    .success(false)
                    .message("Lỗi hệ thống trong quá trình tải: " + e.getMessage())
                    .build());
        }
    }

    /**
     * API Xem lịch sử kiểm toán các lần yêu cầu tải sách của người dùng
     */
    @GetMapping("/logs/user/{userId}")
    public ResponseEntity<List<DownloadLog3>> getUserDownloadLogs(@PathVariable Long userId) {
        return ResponseEntity.ok(downloadLogRepository.findByUserIdOrderByRequestedAtDesc(userId));
    }
}
