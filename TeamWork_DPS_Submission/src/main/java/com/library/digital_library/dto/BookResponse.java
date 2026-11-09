package com.library.digital_library.dto;

import com.library.digital_library.model.Book;
import com.library.digital_library.model.BookPrice;
import java.math.BigDecimal;

public record BookResponse(
        Long id,
        String title,
        String author,
        String category,
        Integer quantity,
        String imageUrl,
        BigDecimal rentalPrice,
        BigDecimal coverPrice,
        Boolean hasEBook) {

    public BookResponse(Long id, String title, String author, String category,
                        Integer quantity, String imageUrl, BigDecimal rentalPrice, BigDecimal coverPrice) {
        this(id, title, author, category, quantity, imageUrl, rentalPrice, coverPrice, false);
    }

    public static BookResponse from(Book book, BookPrice price) {
        return from(book, price, false);
    }

    public static BookResponse from(Book book, BookPrice price, boolean hasEBook) {
        return new BookResponse(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getCategory(),
                book.getQuantity(),
                book.getImageUrl(),
                price == null ? null : price.getRentalPrice(),
                price == null ? null : price.getCoverPrice(),
                hasEBook);
    }
}
