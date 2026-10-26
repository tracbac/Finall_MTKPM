package com.library.digital_library.unittest.unittest3;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.digital_library.controller.controller3.DrmLicenseController3;
import com.library.digital_library.controller.controller3.EBookDownloadController3;
import com.library.digital_library.controller.controller3.LoanController3;
import com.library.digital_library.model.Book;
import com.library.digital_library.model.User;
import com.library.digital_library.model.model3.*;
import com.library.digital_library.repository.repository3.DownloadLog3Repository;
import com.library.digital_library.service.service3.DrmLicenseService3;
import com.library.digital_library.service.service3.EBookDownloadService3;
import com.library.digital_library.service.service3.LoanService3;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("Kiểm thử API Controllers của Thành viên 3 (Loan, DRM & Proxy Download Controller)")
class Controller3Test {

    private MockMvc mockMvcLoans;
    private MockMvc mockMvcDrm;
    private MockMvc mockMvcEbooks;

    @Mock
    private LoanService3 loanService;

    @Mock
    private DrmLicenseService3 drmLicenseService;

    @Mock
    private EBookDownloadService3 eBookDownloadService;

    @Mock
    private DownloadLog3Repository downloadLogRepository;

    @InjectMocks
    private LoanController3 loanController;

    @InjectMocks
    private DrmLicenseController3 drmLicenseController;

    @InjectMocks
    private EBookDownloadController3 eBookDownloadController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvcLoans = MockMvcBuilders.standaloneSetup(loanController).build();
        mockMvcDrm = MockMvcBuilders.standaloneSetup(drmLicenseController).build();
        mockMvcEbooks = MockMvcBuilders.standaloneSetup(eBookDownloadController).build();
    }

    @Test
    @DisplayName("API POST /api/v3/loans/borrow: Trả về HTTP 201 Created khi mượn sách thành công")
    void testApiBorrowBook_Success() throws Exception {
        BorrowRequest3 request = new BorrowRequest3();
        request.setUserId(1L);
        request.setBookId(2L);
        request.setLoanDays(14);

        LoanRecord3 record = LoanRecord3.builder()
                .id(10L)
                .borrowDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(14))
                .status(LoanStatus3.BORROWED)
                .build();

        when(loanService.borrowBook(any(BorrowRequest3.class))).thenReturn(record);

        mockMvcLoans.perform(post("/api/v3/loans/borrow")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.status").value("BORROWED"));
    }

    @Test
    @DisplayName("API POST /api/v3/drm/issue: Trả về HTTP 201 Created khi cấp bản quyền số thành công")
    void testApiIssueDrm_Success() throws Exception {
        IssueDrmRequest3 request = new IssueDrmRequest3();
        request.setUserId(1L);
        request.setBookId(2L);
        request.setValidDays(30);

        DrmLicense3 license = DrmLicense3.builder()
                .id(55L)
                .licenseKey("DRM-TEST-KEY")
                .status("ACTIVE")
                .build();

        when(drmLicenseService.issueLicense(any(IssueDrmRequest3.class))).thenReturn(license);

        mockMvcDrm.perform(post("/api/v3/drm/issue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.licenseKey").value("DRM-TEST-KEY"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("API POST /api/v3/ebooks/download: Trả về HTTP 200 OK khi Proxy cấp quyền tải thành công")
    void testApiDownloadEBook_Allowed() throws Exception {
        DownloadRequest3 request = new DownloadRequest3();
        request.setUserId(1L);
        request.setBookId(2L);
        request.setLicenseKey("DRM-KEY-OK");

        DownloadResult3 result = DownloadResult3.builder()
                .success(true)
                .fileName("book.pdf")
                .downloadUrl("/api/v3/ebooks/stream/token")
                .remainingDownloads(2)
                .build();

        when(eBookDownloadService.downloadEBook(any(DownloadRequest3.class))).thenReturn(result);

        mockMvcEbooks.perform(post("/api/v3/ebooks/download")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.fileName").value("book.pdf"))
                .andExpect(jsonPath("$.remainingDownloads").value(2));
    }

    @Test
    @DisplayName("API POST /api/v3/ebooks/download: Trả về HTTP 403 Forbidden khi Proxy từ chối truy cập")
    void testApiDownloadEBook_ForbiddenByProxy() throws Exception {
        DownloadRequest3 request = new DownloadRequest3();
        request.setUserId(1L);
        request.setBookId(2L);
        request.setLicenseKey("DRM-KEY-BAD");

        when(eBookDownloadService.downloadEBook(any(DownloadRequest3.class)))
                .thenThrow(new SecurityException("[Proxy từ chối] Bạn đang nợ phạt quá hạn!"));

        mockMvcEbooks.perform(post("/api/v3/ebooks/download")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("[Proxy từ chối] Bạn đang nợ phạt quá hạn!"));
    }
}
