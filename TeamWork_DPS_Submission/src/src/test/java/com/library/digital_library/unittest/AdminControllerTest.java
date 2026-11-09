package com.library.digital_library.unittest;

import com.library.digital_library.controller.AdminBookController;
import com.library.digital_library.dto.AdminBookRequest;
import com.library.digital_library.dto.BookResponse;
import com.library.digital_library.service.BookService;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private BookService bookService;

    @InjectMocks
    private AdminBookController adminBookController;

    @Test
    void createsBookWithPricing() {
        AdminBookRequest request = new AdminBookRequest();
        request.setTitle("Clean Code");
        request.setAuthor("Robert C. Martin");
        request.setCategory("Programming");
        request.setQuantity(10);
        request.setCoverPrice(new BigDecimal("25.00"));
        request.setRentalPrice(new BigDecimal("2.50"));
        BookResponse savedBook = new BookResponse(1L, request.getTitle(), request.getAuthor(),
                request.getCategory(), 10, null, request.getRentalPrice(), request.getCoverPrice());
        when(bookService.createBook(any(AdminBookRequest.class))).thenReturn(savedBook);

        ResponseEntity<BookResponse> response = adminBookController.addBook(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(new BigDecimal("2.50"), response.getBody().rentalPrice());
        verify(bookService).createBook(request);
    }

    @Test
    void propagatesServiceFailureForErrorHandler() {
        when(bookService.createBook(any(AdminBookRequest.class)))
                .thenThrow(new IllegalArgumentException("title and author are required"));

        assertThrows(IllegalArgumentException.class,
                () -> adminBookController.addBook(new AdminBookRequest()));
    }
}
