package com.library.digital_library.unittest.unittest3;

import com.library.digital_library.model.Book;
import com.library.digital_library.model.User;
import com.library.digital_library.model.model3.*;
import com.library.digital_library.repository.UserRepository;
import com.library.digital_library.repository.repository3.DownloadLog3Repository;
import com.library.digital_library.repository.repository3.DrmLicense3Repository;
import com.library.digital_library.repository.repository3.FineReceipt3Repository;
import com.library.digital_library.repository.repository3.LoanRecord3Repository;
import com.library.digital_library.service.service3.EBookDownloadProxy3;
import com.library.digital_library.service.service3.RealEBookDownloadService3;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Kiểm thử Design Pattern: Protection Proxy (Kiểm soát quyền tải tài liệu E-Book)")
class ProxyPattern3Test {

    @Mock
    private RealEBookDownloadService3 realService;

    @Mock
    private DrmLicense3Repository drmLicenseRepository;

    @Mock
    private FineReceipt3Repository fineReceiptRepository;

    @Mock
    private LoanRecord3Repository loanRecordRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DownloadLog3Repository downloadLogRepository;

    @InjectMocks
    private EBookDownloadProxy3 downloadProxy;

    private User sampleUser;
    private Book sampleBook;
    private DrmLicense3 validLicense;
    private DownloadRequest3 validRequest;

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setId(1L);
        sampleUser.setUsername("alice");
        sampleUser.setEmail("alice@library.com");

        sampleBook = new Book();
        sampleBook.setId(100L);
        sampleBook.setTitle("Design Patterns & Architecture");

        validLicense = DrmLicense3.builder()
                .id(50L)
                .licenseKey("DRM-VALID-KEY-12345")
                .user(sampleUser)
                .book(sampleBook)
                .issueDate(LocalDate.now().minusDays(5))
                .expiryDate(LocalDate.now().plusDays(25)) // Còn hạn 25 ngày
                .downloadLimit(3)
                .downloadCount(0) // Chưa tải lần nào
                .status("ACTIVE")
                .watermark("Watermark: alice - DRM-VALID-KEY-12345")
                .build();

        validRequest = new DownloadRequest3();
        validRequest.setUserId(1L);
        validRequest.setBookId(100L);
        validRequest.setLicenseKey("DRM-VALID-KEY-12345");
        validRequest.setClientIp("192.168.1.50");
    }

    @Test
    @DisplayName("Proxy cho phép tải khi User có bản quyền hợp lệ, không nợ phạt và còn lượt tải")
    void testDownload_AllowedByProxy() {
        // Giả lập User hợp lệ, không nợ phạt, không có sách quá hạn
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(fineReceiptRepository.countByUserIdAndIsPaidFalse(1L)).thenReturn(0L);
        when(loanRecordRepository.countByUserIdAndStatus(1L, LoanStatus3.OVERDUE)).thenReturn(0L);
        when(drmLicenseRepository.findByLicenseKey("DRM-VALID-KEY-12345")).thenReturn(Optional.of(validLicense));

        // Giả lập Real Subject xử lý tải thành công
        DownloadResult3 realResult = DownloadResult3.builder()
                .success(true)
                .fileName("Design_Patterns_protected.pdf")
                .fileFormat("PDF")
                .fileSize(5242880L)
                .downloadUrl("/api/v3/ebooks/stream/token-abc")
                .build();
        when(realService.downloadEBook(validRequest)).thenReturn(realResult);

        // Gọi qua Proxy
        DownloadResult3 result = downloadProxy.downloadEBook(validRequest);

        // Kiểm tra kết quả
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("Design_Patterns_protected.pdf", result.getFileName());
        assertEquals(validLicense.getWatermark(), result.getDrmWatermark());
        assertEquals(2, result.getRemainingDownloads()); // 3 - 1 = 2 lượt còn lại
        assertEquals(1, validLicense.getDownloadCount()); // Đã tăng lượt tải

        // Kiểm chứng: Proxy ĐÃ ỦY QUYỀN cho Real Subject thực hiện
        verify(realService, times(1)).downloadEBook(validRequest);
        // Kiểm chứng: Proxy ĐÃ LƯU bản ghi kiểm toán SUCCESS
        verify(downloadLogRepository, times(1)).save(argThat(log -> "SUCCESS".equals(log.getStatus())));
    }

    @Test
    @DisplayName("Proxy CHẶN tải khi người dùng đang có nợ phạt quá hạn chưa thanh toán")
    void testProxy_DeniesWhenUserHasUnpaidFines() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(fineReceiptRepository.countByUserIdAndIsPaidFalse(1L)).thenReturn(1L); // Có 1 khoản nợ phạt

        SecurityException ex = assertThrows(SecurityException.class, () -> downloadProxy.downloadEBook(validRequest));

        assertTrue(ex.getMessage().contains("nợ phạt quá hạn"));
        // Proxy ngăn chặn, KHÔNG ủy quyền cho Real Subject
        verify(realService, never()).downloadEBook(any());
        // Proxy ghi log DENIED
        verify(downloadLogRepository, times(1)).save(argThat(log -> "DENIED".equals(log.getStatus())));
    }

    @Test
    @DisplayName("Proxy CHẶN tải khi người dùng đang có sách mượn quá hạn chưa trả")
    void testProxy_DeniesWhenUserHasOverdueLoans() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(fineReceiptRepository.countByUserIdAndIsPaidFalse(1L)).thenReturn(0L);
        when(loanRecordRepository.countByUserIdAndStatus(1L, LoanStatus3.OVERDUE)).thenReturn(2L); // 2 sách quá hạn

        SecurityException ex = assertThrows(SecurityException.class, () -> downloadProxy.downloadEBook(validRequest));

        assertTrue(ex.getMessage().contains("quá hạn chưa hoàn trả"));
        verify(realService, never()).downloadEBook(any());
        verify(downloadLogRepository, times(1)).save(argThat(log -> "DENIED".equals(log.getStatus())));
    }

    @Test
    @DisplayName("Proxy CHẶN tải khi không có mã bản quyền DRM hợp lệ")
    void testProxy_DeniesWhenLicenseNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(fineReceiptRepository.countByUserIdAndIsPaidFalse(1L)).thenReturn(0L);
        when(loanRecordRepository.countByUserIdAndStatus(1L, LoanStatus3.OVERDUE)).thenReturn(0L);
        when(drmLicenseRepository.findByLicenseKey("DRM-VALID-KEY-12345")).thenReturn(Optional.empty());

        SecurityException ex = assertThrows(SecurityException.class, () -> downloadProxy.downloadEBook(validRequest));

        assertTrue(ex.getMessage().contains("không tồn tại hoặc không hợp lệ"));
        verify(realService, never()).downloadEBook(any());
        verify(downloadLogRepository, times(1)).save(argThat(log -> "DENIED".equals(log.getStatus())));
    }

    @Test
    @DisplayName("Proxy CHẶN tải khi giấy phép DRM đã hết hạn sử dụng")
    void testProxy_DeniesWhenLicenseExpired() {
        validLicense.setExpiryDate(LocalDate.now().minusDays(1)); // Đã hết hạn hôm qua

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(fineReceiptRepository.countByUserIdAndIsPaidFalse(1L)).thenReturn(0L);
        when(loanRecordRepository.countByUserIdAndStatus(1L, LoanStatus3.OVERDUE)).thenReturn(0L);
        when(drmLicenseRepository.findByLicenseKey("DRM-VALID-KEY-12345")).thenReturn(Optional.of(validLicense));

        SecurityException ex = assertThrows(SecurityException.class, () -> downloadProxy.downloadEBook(validRequest));

        assertTrue(ex.getMessage().contains("hết hạn"));
        assertEquals("EXPIRED", validLicense.getStatus());
        verify(realService, never()).downloadEBook(any());
        verify(downloadLogRepository, times(1)).save(argThat(log -> "DENIED".equals(log.getStatus())));
    }

    @Test
    @DisplayName("Proxy CHẶN tải khi giấy phép DRM đã bị thu hồi (REVOKED)")
    void testProxy_DeniesWhenLicenseRevoked() {
        validLicense.setStatus("REVOKED");

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(fineReceiptRepository.countByUserIdAndIsPaidFalse(1L)).thenReturn(0L);
        when(loanRecordRepository.countByUserIdAndStatus(1L, LoanStatus3.OVERDUE)).thenReturn(0L);
        when(drmLicenseRepository.findByLicenseKey("DRM-VALID-KEY-12345")).thenReturn(Optional.of(validLicense));

        SecurityException ex = assertThrows(SecurityException.class, () -> downloadProxy.downloadEBook(validRequest));

        assertTrue(ex.getMessage().contains("thu hồi"));
        verify(realService, never()).downloadEBook(any());
        verify(downloadLogRepository, times(1)).save(argThat(log -> "DENIED".equals(log.getStatus())));
    }

    @Test
    @DisplayName("Proxy CHẶN tải khi đã đạt giới hạn số lần tải tối đa (Download Quota)")
    void testProxy_DeniesWhenDownloadQuotaExceeded() {
        validLicense.setDownloadLimit(3);
        validLicense.setDownloadCount(3); // Đã tải đủ 3 lần

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(fineReceiptRepository.countByUserIdAndIsPaidFalse(1L)).thenReturn(0L);
        when(loanRecordRepository.countByUserIdAndStatus(1L, LoanStatus3.OVERDUE)).thenReturn(0L);
        when(drmLicenseRepository.findByLicenseKey("DRM-VALID-KEY-12345")).thenReturn(Optional.of(validLicense));

        SecurityException ex = assertThrows(SecurityException.class, () -> downloadProxy.downloadEBook(validRequest));

        assertTrue(ex.getMessage().contains("tối đa cho phép"));
        verify(realService, never()).downloadEBook(any());
        verify(downloadLogRepository, times(1)).save(argThat(log -> "DENIED".equals(log.getStatus())));
    }
}
