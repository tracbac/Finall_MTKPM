package com.library.digital_library.service;

import com.library.digital_library.dto.AdminBookRequest;
import com.library.digital_library.dto.BookResponse;
import com.library.digital_library.model.Book;
import com.library.digital_library.model.BookPrice;
import com.library.digital_library.repository.BookRepository;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final BookPriceService bookPriceService;
    private final BookCoverStorage bookCoverStorage;

    public BookService(
            BookRepository bookRepository,
            BookPriceService bookPriceService,
            BookCoverStorage bookCoverStorage) {
        this.bookRepository = bookRepository;
        this.bookPriceService = bookPriceService;
        this.bookCoverStorage = bookCoverStorage;
    }

    @Transactional(readOnly = true)
    public List<BookResponse> getBooks(String title, String category) {
        List<Book> books = bookRepository.search(normalizeFilter(title), normalizeFilter(category));
        Map<Long, BookPrice> prices = bookPriceService.findByBookIds(
                books.stream().map(Book::getId).toList());
        return books.stream()
                .map(book -> BookResponse.from(book, prices.get(book.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<BookResponse> getBookById(Long id) {
        return bookRepository.findById(id)
                .map(book -> BookResponse.from(
                        book,
                        bookPriceService.findByBookIds(List.of(id)).get(id)));
    }

    @Transactional
    public BookResponse createBook(AdminBookRequest request) {
        validateBook(request);
        Book book = new Book();
        applyRequest(book, request);
        if (request.getImageFile() != null && !request.getImageFile().isEmpty()) {
            book.setImageUrl(bookCoverStorage.upload(request.getImageFile()));
        }

        Book savedBook = bookRepository.save(book);
        BookPrice price = bookPriceService.save(
                savedBook, request.getRentalPrice(), request.getCoverPrice());
        return BookResponse.from(savedBook, price);
    }

    @Transactional
    public BookResponse updateBook(Long id, AdminBookRequest request) {
        validateBook(request);
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Book not found"));
        applyRequest(book, request);
        if (request.getImageFile() != null && !request.getImageFile().isEmpty()) {
            book.setImageUrl(bookCoverStorage.upload(request.getImageFile()));
        }

        Book savedBook = bookRepository.save(book);
        BookPrice price = bookPriceService.save(
                savedBook, request.getRentalPrice(), request.getCoverPrice());
        return BookResponse.from(savedBook, price);
    }

    @Transactional
    public void deleteBook(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new NoSuchElementException("Book not found");
        }
        bookPriceService.deleteForBook(id);
        bookRepository.deleteById(id);
    }

    private void applyRequest(Book book, AdminBookRequest request) {
        book.setTitle(request.getTitle().trim());
        book.setAuthor(request.getAuthor().trim());
        book.setCategory(StringUtils.hasText(request.getCategory()) ? request.getCategory().trim() : null);
        book.setQuantity(request.getQuantity());
    }

    private void validateBook(AdminBookRequest request) {
        if (request == null
                || !StringUtils.hasText(request.getTitle())
                || !StringUtils.hasText(request.getAuthor())) {
            throw new IllegalArgumentException("title and author are required");
        }
        if (request.getQuantity() == null || request.getQuantity() < 0) {
            throw new IllegalArgumentException("quantity must not be negative");
        }
        if (request.getRentalPrice() == null || request.getCoverPrice() == null
                || request.getRentalPrice().signum() < 0 || request.getCoverPrice().signum() < 0) {
            throw new IllegalArgumentException("rentalPrice and coverPrice must be non-negative");
        }
    }

    private String normalizeFilter(String value) {
        return StringUtils.hasText(value) ? value.trim() : "";
    }
}
