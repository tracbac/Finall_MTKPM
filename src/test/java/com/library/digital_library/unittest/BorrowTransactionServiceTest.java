package com.library.digital_library.unittest;

import com.library.digital_library.model.Book;
import com.library.digital_library.model.BorrowTransaction;
import com.library.digital_library.model.User;
import com.library.digital_library.repository.BookRepository;
import com.library.digital_library.repository.BorrowTransactionRepository;
import com.library.digital_library.repository.UserRepository;
import com.library.digital_library.service.BorrowTransactionService;
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
@DisplayName("Kiểm thử logic Mượn sách (Borrow Transaction Service Test)")
class BorrowTransactionServiceTest {

    @Mock
    private BorrowTransactionRepository transactionRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BorrowTransactionService transactionService;

    private User sampleUser;
    private Book sampleBook;

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setId(1L);
        sampleUser.setUsername("bacnguyen");

        sampleBook = new Book();
        sampleBook.setId(1L);
        sampleBook.setTitle("Clean Code");
        sampleBook.setQuantity(5);
    }

    @Test
    @DisplayName("Test mượn sách thành công")
    void testBorrowBookSuccess() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(sampleBook));
        when(transactionRepository.save(any(BorrowTransaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BorrowTransaction transaction = transactionService.borrowBook(1L, 1L);

        assertNotNull(transaction);
        assertEquals("BORROWED", transaction.getStatus());
        assertEquals(4, sampleBook.getQuantity()); // Kiểm tra kho giảm đi 1
        verify(bookRepository, times(1)).save(sampleBook);
        verify(transactionRepository, times(1)).save(any(BorrowTransaction.class));
    }
}