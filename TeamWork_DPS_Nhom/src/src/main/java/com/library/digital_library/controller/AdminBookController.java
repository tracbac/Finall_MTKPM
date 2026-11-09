package com.library.digital_library.controller;

import com.library.digital_library.dto.AdminBookRequest;
import com.library.digital_library.dto.BookResponse;
import com.library.digital_library.service.BookService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/books")
@RequiredArgsConstructor
public class AdminBookController {

    private final BookService bookService;

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<BookResponse> addBook(@Valid @ModelAttribute AdminBookRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookService.createBook(request));
    }

    @PutMapping(path = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<BookResponse> updateBook(
            @PathVariable Long id,
            @Valid @ModelAttribute AdminBookRequest request) {
        return ResponseEntity.ok(bookService.updateBook(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }
}