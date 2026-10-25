package com.library.digital_library.unittest;

import com.library.digital_library.model.Book;
import com.library.digital_library.repository.BookRepository;
import com.library.digital_library.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Kiểm thử logic Quản lý Sách (Book Service Unit Test)")
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

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
    @DisplayName("Test lấy danh sách tất cả sách thành công")
    void testGetAllBooks() {
        when(bookRepository.findAll()).thenReturn(Arrays.asList(sampleBook));

        List<Book> books = bookService.getAllBooks();

        assertEquals(1, books.size());
        assertEquals("Lập trình Spring Boot", books.get(0).getTitle());
        verify(bookRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Test thêm mới sách thành công")
    void testSaveBook() {
        when(bookRepository.save(any(Book.class))).thenReturn(sampleBook);

        Book savedBook = bookService.saveBook(sampleBook);

        assertNotNull(savedBook);
        assertEquals("Lập trình Spring Boot", savedBook.getTitle());
        verify(bookRepository, times(1)).save(sampleBook);
    }
}