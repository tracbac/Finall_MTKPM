package com.library.digital_library.unittest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.digital_library.controller.BookController;
import com.library.digital_library.model.Book;
import com.library.digital_library.service.BookService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookController.class)
@DisplayName("Kiểm thử API Quản lý Sách (Book Controller Test)")
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Test API lấy danh sách sách thành công")
    void testGetAllBooksEndpoint() throws Exception {
        Book book = new Book();
        book.setId(1L);
        book.setTitle("Clean Architecture");
        book.setAuthor("Robert C. Martin");
        book.setCategory("IT");
        book.setQuantity(5);

        when(bookService.getAllBooks()).thenReturn(Arrays.asList(book));

        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Clean Architecture"));
    }
}