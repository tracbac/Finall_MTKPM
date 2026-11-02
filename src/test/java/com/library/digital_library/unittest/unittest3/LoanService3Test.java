package com.library.digital_library.unittest.unittest3;

import com.library.digital_library.model.Book;
import com.library.digital_library.model.User;
import com.library.digital_library.model.model3.*;
import com.library.digital_library.repository.BookRepository;
import com.library.digital_library.repository.UserRepository;
import com.library.digital_library.repository.repository3.FineReceipt3Repository;
import com.library.digital_library.repository.repository3.LoanRecord3Repository;
import com.library.digital_library.service.service3.LoanService3;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Kiểm thử Nghiệp vụ Mượn/Trả, Gia hạn & Phạt quá hạn (Thành viên 3)")
class LoanService3Test {

    @Mock
    private LoanRecord3Repository loanRecordRepository;

    @Mock
    private FineReceipt3Repository fineReceiptRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private LoanService3 loanService;

    private User sampleUser;
    private Book sampleBook;

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setId(10L);
        sampleUser.setUsername("testuser3");
        sampleUser.setEmail("user3@library.com");

        sampleBook = new Book();
        sampleBook.setId(20L);
        sampleBook.setTitle("Design Patterns in Java");
        sampleBook.setQuantity(5);
    }

    @Test
    @DisplayName("Mượn sách thành công - Tạo phiếu mượn và giảm tồn kho")
    void testBorrowBook_Success() {
        when(userRepository.findById(10L)).thenReturn(Optional.of(sampleUser));
        when(fineReceiptRepository.countByUserIdAndIsPaidFalse(10L)).thenReturn(0L);
        when(loanRecordRepository.countByUserIdAndStatus(10L, LoanStatus3.OVERDUE)).thenReturn(0L);
        when(bookRepository.findById(20L)).thenReturn(Optional.of(sampleBook));
        when(loanRecordRepository.save(any(LoanRecord3.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BorrowRequest3 request = new BorrowRequest3();
        request.setUserId(10L);
        request.setBookId(20L);
        request.setLoanDays(14);

        LoanRecord3 result = loanService.borrowBook(request);

        assertNotNull(result);
        assertEquals(LoanStatus3.BORROWED, result.getStatus());
        assertEquals(4, sampleBook.getQuantity()); // Số lượng sách đã giảm từ 5 xuống 4
        assertEquals(LocalDate.now().plusDays(14), result.getDueDate());
        verify(bookRepository, times(1)).save(sampleBook);
        verify(loanRecordRepository, times(1)).save(any(LoanRecord3.class));
    }

    @Test
    @DisplayName("Chặn mượn sách khi người dùng đang có nợ phạt quá hạn chưa trả")
    void testBorrowBook_BlockedDueToUnpaidFines() {
        when(userRepository.findById(10L)).thenReturn(Optional.of(sampleUser));
        when(fineReceiptRepository.countByUserIdAndIsPaidFalse(10L)).thenReturn(2L); // Có 2 khoản phạt

        BorrowRequest3 request = new BorrowRequest3();
        request.setUserId(10L);
        request.setBookId(20L);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> loanService.borrowBook(request));
        assertTrue(ex.getMessage().contains("khoản phạt quá hạn chưa thanh toán"));
        verify(bookRepository, never()).save(any());
        verify(loanRecordRepository, never()).save(any());
    }

    @Test
    @DisplayName("Chặn mượn sách khi người dùng đang giữ sách quá hạn")
    void testBorrowBook_BlockedDueToOverdueLoans() {
        when(userRepository.findById(10L)).thenReturn(Optional.of(sampleUser));
        when(fineReceiptRepository.countByUserIdAndIsPaidFalse(10L)).thenReturn(0L);
        when(loanRecordRepository.countByUserIdAndStatus(10L, LoanStatus3.OVERDUE)).thenReturn(1L); // Có sách quá hạn

        BorrowRequest3 request = new BorrowRequest3();
        request.setUserId(10L);
        request.setBookId(20L);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> loanService.borrowBook(request));
        assertTrue(ex.getMessage().contains("sách mượn quá hạn chưa trả"));
    }

    @Test
    @DisplayName("Trả sách đúng hạn - Trả lại kho và không bị phạt")
    void testReturnBook_OnTime() {
        LoanRecord3 loan = LoanRecord3.builder()
                .id(1L)
                .user(sampleUser)
                .book(sampleBook)
                .borrowDate(LocalDate.now().minusDays(10))
                .dueDate(LocalDate.now().plusDays(4)) // Chưa tới hạn
                .status(LoanStatus3.BORROWED)
                .fineAmount(0.0)
                .build();

        when(loanRecordRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(loanRecordRepository.save(any(LoanRecord3.class))).thenAnswer(invocation -> invocation.getArgument(0));

        int initialQty = sampleBook.getQuantity();
        LoanRecord3 returnedLoan = loanService.returnBook(1L);

        assertEquals(LoanStatus3.RETURNED, returnedLoan.getStatus());
        assertEquals(initialQty + 1, sampleBook.getQuantity()); // Tồn kho tăng thêm 1
        assertEquals(0.0, returnedLoan.getFineAmount());
        verify(fineReceiptRepository, never()).save(any());
    }

    @Test
    @DisplayName("Trả sách trễ hạn - Tự động tính tiền phạt và lập biên lai phạt")
    void testReturnBook_OverdueWithFine() {
        // Mượn từ 20 ngày trước, hạn trả là 6 ngày trước -> trễ 6 ngày
        LocalDate dueDate = LocalDate.now().minusDays(6);
        LoanRecord3 loan = LoanRecord3.builder()
                .id(2L)
                .user(sampleUser)
                .book(sampleBook)
                .borrowDate(dueDate.minusDays(14))
                .dueDate(dueDate)
                .status(LoanStatus3.BORROWED)
                .build();

        when(loanRecordRepository.findById(2L)).thenReturn(Optional.of(loan));
        when(loanRecordRepository.save(any(LoanRecord3.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LoanRecord3 returnedLoan = loanService.returnBook(2L);

        assertEquals(LoanStatus3.RETURNED, returnedLoan.getStatus());
        // 6 ngày trễ * 5000 = 30000 VNĐ
        assertEquals(30000.0, returnedLoan.getFineAmount());
        assertFalse(returnedLoan.getFinePaid());
        verify(fineReceiptRepository, times(1)).save(any(FineReceipt3.class));
    }

    @Test
    @DisplayName("Gia hạn mượn sách thành công khi còn trong hạn và chưa quá số lần")
    void testRenewLoan_Success() {
        LocalDate currentDue = LocalDate.now().plusDays(3);
        LoanRecord3 loan = LoanRecord3.builder()
                .id(3L)
                .user(sampleUser)
                .book(sampleBook)
                .dueDate(currentDue)
                .renewalCount(0)
                .maxRenewals(2)
                .status(LoanStatus3.BORROWED)
                .build();

        when(loanRecordRepository.findById(3L)).thenReturn(Optional.of(loan));
        when(loanRecordRepository.save(any(LoanRecord3.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RenewRequest3 renewReq = new RenewRequest3();
        renewReq.setExtraDays(7);

        LoanRecord3 result = loanService.renewLoan(3L, renewReq);

        assertEquals(LoanStatus3.EXTENDED, result.getStatus());
        assertEquals(1, result.getRenewalCount());
        assertEquals(currentDue.plusDays(7), result.getDueDate());
    }

    @Test
    @DisplayName("Từ chối gia hạn khi sách đã quá hạn trả")
    void testRenewLoan_RejectedWhenOverdue() {
        LocalDate pastDue = LocalDate.now().minusDays(2);
        LoanRecord3 loan = LoanRecord3.builder()
                .id(4L)
                .dueDate(pastDue)
                .renewalCount(0)
                .maxRenewals(2)
                .status(LoanStatus3.BORROWED)
                .build();

        when(loanRecordRepository.findById(4L)).thenReturn(Optional.of(loan));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> loanService.renewLoan(4L, new RenewRequest3()));
        assertTrue(ex.getMessage().contains("quá hạn trả"));
    }

    @Test
    @DisplayName("Từ chối gia hạn khi đã đạt số lần gia hạn tối đa (2 lần)")
    void testRenewLoan_RejectedWhenExceedMaxRenewals() {
        LocalDate currentDue = LocalDate.now().plusDays(2);
        LoanRecord3 loan = LoanRecord3.builder()
                .id(5L)
                .dueDate(currentDue)
                .renewalCount(2) // Đã gia hạn 2 lần
                .maxRenewals(2)
                .status(LoanStatus3.EXTENDED)
                .build();

        when(loanRecordRepository.findById(5L)).thenReturn(Optional.of(loan));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> loanService.renewLoan(5L, new RenewRequest3()));
        assertTrue(ex.getMessage().contains("vượt quá số lần gia hạn cho phép"));
    }

    @Test
    @DisplayName("Nộp phạt thành công - Cập nhật trạng thái biên lai và phiếu mượn")
    void testPayFine_Success() {
        LoanRecord3 loan = LoanRecord3.builder()
                .id(6L)
                .fineAmount(20000.0)
                .finePaid(false)
                .build();

        FineReceipt3 fine = FineReceipt3.builder()
                .id(100L)
                .loanRecord(loan)
                .user(sampleUser)
                .totalFine(20000.0)
                .isPaid(false)
                .build();

        when(fineReceiptRepository.findById(100L)).thenReturn(Optional.of(fine));
        when(fineReceiptRepository.save(any(FineReceipt3.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(fineReceiptRepository.findByLoanRecordId(6L)).thenReturn(Collections.singletonList(fine));

        FinePaymentRequest3 payReq = new FinePaymentRequest3();
        payReq.setPaymentMethod("BANKING");

        FineReceipt3 paidFine = loanService.payFine(100L, payReq);

        assertTrue(paidFine.getIsPaid());
        assertEquals("BANKING", paidFine.getPaymentMethod());
        assertNotNull(paidFine.getPaidDate());
        assertTrue(loan.getFinePaid());
        verify(loanRecordRepository, times(1)).save(loan);
    }
}
