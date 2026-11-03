package com.library.digital_library.unittest.unittest3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.argThat;

import com.library.digital_library.model.Book;
import com.library.digital_library.model.User;
import com.library.digital_library.model.model3.DrmLicense3;
import com.library.digital_library.model.model3.EBookBorrowRequest3;
import com.library.digital_library.model.model3.EBookRequestStatus3;
import com.library.digital_library.repository.BookRepository;
import com.library.digital_library.repository.UserRepository;
import com.library.digital_library.repository.repository3.DrmLicense3Repository;
import com.library.digital_library.repository.repository3.EBookBorrowRequest3Repository;
import com.library.digital_library.service.service3.DrmLicenseService3;
import com.library.digital_library.service.service3.EBookBorrowRequest3Service;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EBookBorrowRequest3ServiceTest {

    @Mock
    private EBookBorrowRequest3Repository requestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private DrmLicense3Repository drmLicenseRepository;

    @Mock
    private DrmLicenseService3 drmLicenseService;

    @InjectMocks
    private EBookBorrowRequest3Service requestService;

    private User user;
    private Book book;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(7L);
        user.setUsername("reader");

        book = new Book();
        book.setId(12L);
        book.setTitle("Digital Book");
    }

    @Test
    void createsPendingRequestForAuthenticatedUsername() {
        when(userRepository.findByUsername("reader")).thenReturn(Optional.of(user));
        when(bookRepository.findById(12L)).thenReturn(Optional.of(book));
        when(requestRepository.existsByUserIdAndBookIdAndStatus(7L, 12L, EBookRequestStatus3.PENDING))
                .thenReturn(false);
        when(drmLicenseRepository.findFirstByUserIdAndBookIdAndStatus(7L, 12L, "ACTIVE"))
                .thenReturn(Optional.empty());
        when(requestRepository.save(any(EBookBorrowRequest3.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        EBookBorrowRequest3 request = requestService.createRequest("reader", 12L);

        assertEquals(user, request.getUser());
        assertEquals(book, request.getBook());
        assertEquals(EBookRequestStatus3.PENDING, request.getStatus());
        verify(requestRepository).save(any(EBookBorrowRequest3.class));
    }

    @Test
    void preventsDuplicatePendingRequest() {
        when(userRepository.findByUsername("reader")).thenReturn(Optional.of(user));
        when(bookRepository.findById(12L)).thenReturn(Optional.of(book));
        when(requestRepository.existsByUserIdAndBookIdAndStatus(7L, 12L, EBookRequestStatus3.PENDING))
                .thenReturn(true);

        assertThrows(IllegalStateException.class, () -> requestService.createRequest("reader", 12L));
        verify(requestRepository, never()).save(any(EBookBorrowRequest3.class));
    }

    @Test
    void approvalIssuesLicenseAndStoresLicenseKey() {
        EBookBorrowRequest3 request = EBookBorrowRequest3.builder()
                .id(5L)
                .user(user)
                .book(book)
                .status(EBookRequestStatus3.PENDING)
                .build();
        DrmLicense3 license = DrmLicense3.builder().licenseKey("DRM-ISSUED").build();
        when(requestRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(request));
        when(drmLicenseService.issueLicense(any())).thenReturn(license);
        when(requestRepository.save(any(EBookBorrowRequest3.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        EBookBorrowRequest3 approved = requestService.approveRequest(5L, 45, 6);

        assertEquals(EBookRequestStatus3.APPROVED, approved.getStatus());
        assertEquals("DRM-ISSUED", approved.getLicenseKey());
        assertEquals("Đã duyệt: hiệu lực 45 ngày, tối đa 6 lượt tải.", approved.getReviewNote());
        verify(drmLicenseService).issueLicense(argThat(issue ->
                issue.getValidDays() == 45 && issue.getMaxDownloads() == 6));
    }

    @Test
    void cannotApproveRequestTwice() {
        EBookBorrowRequest3 request = EBookBorrowRequest3.builder()
                .id(5L)
                .user(user)
                .book(book)
                .status(EBookRequestStatus3.APPROVED)
                .build();
        when(requestRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(request));

        assertThrows(IllegalStateException.class, () -> requestService.approveRequest(5L, 30, 3));
        verify(drmLicenseService, never()).issueLicense(any());
    }

    @Test
    void approvalRequiresPositiveLicenseSettings() {
        EBookBorrowRequest3 request = EBookBorrowRequest3.builder()
                .id(5L)
                .user(user)
                .book(book)
                .status(EBookRequestStatus3.PENDING)
                .build();
        when(requestRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(request));

        assertThrows(IllegalArgumentException.class, () -> requestService.approveRequest(5L, 0, 3));
        verify(drmLicenseService, never()).issueLicense(any());
    }
}
