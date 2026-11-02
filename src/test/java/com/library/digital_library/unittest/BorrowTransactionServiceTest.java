package com.library.digital_library.unittest;

import com.library.digital_library.dto.BorrowRequest;
import com.library.digital_library.model.Book;
import com.library.digital_library.model.BorrowTransaction;
import com.library.digital_library.model.User;
import com.library.digital_library.repository.BookRepository;
import com.library.digital_library.repository.BorrowTransactionRepository;
import com.library.digital_library.repository.UserRepository;
import com.library.digital_library.service.BorrowTransactionService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BorrowTransactionServiceTest {

    @Mock
    private BorrowTransactionRepository transactionRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BorrowTransactionService transactionService;

    private User user;
    private Book book;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("reader");

        book = new Book();
        book.setId(10L);
        book.setTitle("Clean Code");
        book.setQuantity(5);
    }

    @Test
    void createsPendingRequestAndReservesOneCopy() {
        BorrowRequest request = request(10L, 14);
        when(userRepository.findByUsername("reader")).thenReturn(Optional.of(user));
        when(bookRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(book));
        when(transactionRepository.save(any(BorrowTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BorrowTransaction transaction = transactionService.createBorrowRequest("reader", request);

        assertEquals("PENDING", transaction.getStatus());
        assertEquals(4, book.getQuantity());
        assertEquals(transaction.getBorrowDate().plusDays(14), transaction.getDueDate());
    }

    @Test
    void rejectsBorrowWhenInventoryIsEmpty() {
        book.setQuantity(0);
        when(userRepository.findByUsername("reader")).thenReturn(Optional.of(user));
        when(bookRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(book));

        assertThrows(IllegalStateException.class,
                () -> transactionService.createBorrowRequest("reader", request(10L, 14)));
        verify(transactionRepository, never()).save(any(BorrowTransaction.class));
    }

    @Test
    void approvalAndReturnFollowValidStateTransitions() {
        book.setQuantity(4);
        BorrowTransaction transaction = transaction("PENDING");
        when(transactionRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any(BorrowTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(bookRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(book));

        assertEquals("BORROWED", transactionService.approveTransaction(20L).getStatus());
        assertEquals("RETURNED", transactionService.returnBook(20L).getStatus());
        assertEquals(5, book.getQuantity());
    }

    @Test
    void onlyOwnerCanCancelPendingRequestAndInventoryIsReleased() {
        BorrowTransaction transaction = transaction("PENDING");
        when(transactionRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(transaction));
        when(bookRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(book));
        when(transactionRepository.save(any(BorrowTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        book.setQuantity(4);

        BorrowTransaction canceled = transactionService.cancelBorrowRequest("reader", 20L);

        assertEquals("CANCELED", canceled.getStatus());
        assertEquals(5, book.getQuantity());
    }

    @Test
    void rejectsPendingRequestAndReleasesReservedCopy() {
        book.setQuantity(4);
        BorrowTransaction transaction = transaction("PENDING");
        when(transactionRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(transaction));
        when(bookRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(book));
        when(transactionRepository.save(any(BorrowTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BorrowTransaction rejected = transactionService.rejectBorrowRequest(20L);

        assertEquals("REJECTED", rejected.getStatus());
        assertEquals(5, book.getQuantity());
    }

    @Test
    void preventsAnotherUserFromCancelingBorrowRequest() {
        BorrowTransaction transaction = transaction("PENDING");
        when(transactionRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(transaction));

        assertThrows(AccessDeniedException.class,
                () -> transactionService.cancelBorrowRequest("someone-else", 20L));
        verify(bookRepository, never()).findByIdForUpdate(10L);
    }

    private BorrowRequest request(Long bookId, int days) {
        BorrowRequest request = new BorrowRequest();
        request.setBookId(bookId);
        request.setBorrowDays(days);
        return request;
    }

    private BorrowTransaction transaction(String status) {
        BorrowTransaction transaction = new BorrowTransaction();
        transaction.setId(20L);
        transaction.setUser(user);
        transaction.setBook(book);
        transaction.setStatus(status);
        return transaction;
    }
}
