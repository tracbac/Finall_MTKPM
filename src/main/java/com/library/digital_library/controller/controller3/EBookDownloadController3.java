package com.library.digital_library.controller.controller3;

import com.library.digital_library.model.model3.DownloadLog3;
import com.library.digital_library.model.model3.DownloadRequest3;
import com.library.digital_library.model.model3.DownloadResult3;
import com.library.digital_library.repository.repository3.DownloadLog3Repository;
import com.library.digital_library.repository.UserRepository;
import com.library.digital_library.service.EBookStorageException;
import com.library.digital_library.service.service3.EBookDownloadService3;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ContentDisposition;
import java.nio.charset.StandardCharsets;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.security.Principal;

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
    private final UserRepository userRepository;

    /**
     * API Tải E-Book - Được kiểm soát hoàn toàn bởi Protection Proxy
     * - Chỉ người dùng có bản quyền hợp lệ, không nợ phạt mới được tải
     * - Proxy sẽ chặn và ném SecurityException nếu không đủ thẩm quyền (Trả về HTTP 403 FORBIDDEN)
     */
    @PostMapping("/download")
    public ResponseEntity<?> downloadEBook(
            @Valid @RequestBody DownloadRequest3 request,
            Principal principal,
            HttpServletRequest httpRequest) {
        try {
            Long userId = userRepository.findByUsername(principal.getName())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng đăng nhập."))
                    .getId();
            request.setUserId(userId);
            if (request.getClientIp() == null || request.getClientIp().isEmpty()) {
                request.setClientIp(httpRequest.getRemoteAddr());
            }

            DownloadResult3 result = eBookDownloadService.downloadEBook(request);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(
                    ContentDisposition.attachment().filename(result.getFileName(), StandardCharsets.UTF_8).build());
            headers.setContentLength(result.getFileContent().length);
            headers.setCacheControl("no-store, no-cache, must-revalidate");
            headers.setPragma("no-cache");
            headers.set("X-Remaining-Downloads", String.valueOf(result.getRemainingDownloads()));
            return ResponseEntity.ok().headers(headers).body(result.getFileContent());

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
        } catch (EBookStorageException e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(DownloadResult3.builder()
                    .success(false)
                    .message("Kho lưu trữ PDF hiện không thể cấp tệp tải xuống.")
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

    @GetMapping("/logs/mine")
    public ResponseEntity<List<DownloadLog3>> getMyDownloadLogs(Principal principal) {
        Long userId = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng đăng nhập."))
                .getId();
        return ResponseEntity.ok(downloadLogRepository.findByUserIdOrderByRequestedAtDesc(userId));
    }
}
