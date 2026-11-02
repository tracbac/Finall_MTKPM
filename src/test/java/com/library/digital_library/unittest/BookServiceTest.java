package com.library.digital_library.unittest;

import com.library.digital_library.model.Book;
import com.library.digital_library.model.BookPrice;
import com.library.digital_library.repository.BookRepository;
import com.library.digital_library.service.BookCoverStorage;
import com.library.digital_library.service.BookPriceService;
import com.library.digital_library.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Kiểm thử logic Quản lý Sách (Book Service Unit Test)")
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookPriceService bookPriceService;

    @Mock
    private BookCoverStorage bookCoverStorage;

    @InjectMocks
    private BookService bookService;

    private Book sampleBook;

   @BeforeEach
    void setUp() {
        sampleBook = new Book();
        sampleBook.setId(1L);
        sampleBook.setTitle("Lập trình Spring Boot");   // Xóa bỏ "title: "
        sampleBook.setAuthor("Nguyen Trac Bac");        // Xóa bỏ "author: "
        sampleBook.setCategory("IT");                   // Xóa bỏ "category: "
        sampleBook.setQuantity(10);                        // Xóa bỏ "stock: " (hoặc quantity tùy thuộc vào entity Book của bạn)
    }

    @Test
    void filtersBooksAndIncludesPersistedPrices() {
        BookPrice price = new BookPrice();
        price.setBook(sampleBook);
        price.setRentalPrice(new java.math.BigDecimal("3.00"));
        price.setCoverPrice(new java.math.BigDecimal("25.00"));
        when(bookRepository.search("Spring", "IT")).thenReturn(List.of(sampleBook));
        when(bookPriceService.findByBookIds(List.of(1L))).thenReturn(Map.of(1L, price));

        var result = bookService.getBooks(" Spring ", " IT ");

        assertEquals(1, result.size());
        assertEquals(new java.math.BigDecimal("3.00"), result.get(0).rentalPrice());
        assertEquals(new java.math.BigDecimal("25.00"), result.get(0).coverPrice());
        verify(bookRepository).search("Spring", "IT");
    }
}