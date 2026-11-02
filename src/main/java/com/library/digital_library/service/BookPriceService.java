package com.library.digital_library.service;

import com.library.digital_library.model.Book;
import com.library.digital_library.model.BookPrice;
import com.library.digital_library.repository.BookPriceRepository;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookPriceService {

    private final BookPriceRepository bookPriceRepository;

    public BookPriceService(BookPriceRepository bookPriceRepository) {
        this.bookPriceRepository = bookPriceRepository;
    }

    @Transactional
    public BookPrice save(Book book, BigDecimal rentalPrice, BigDecimal coverPrice) {
        validatePrices(rentalPrice, coverPrice);
        BookPrice price = bookPriceRepository.findByBook_Id(book.getId()).orElseGet(BookPrice::new);
        price.setBook(book);
        price.setRentalPrice(rentalPrice);
        price.setCoverPrice(coverPrice);
        return bookPriceRepository.save(price);
    }

    @Transactional(readOnly = true)
    public Map<Long, BookPrice> findByBookIds(Collection<Long> bookIds) {
        if (bookIds.isEmpty()) {
            return Map.of();
        }
        return bookPriceRepository.findAllByBook_IdIn(bookIds).stream()
                .collect(Collectors.toMap(price -> price.getBook().getId(), Function.identity()));
    }

    @Transactional
    public void deleteForBook(Long bookId) {
        bookPriceRepository.deleteByBook_Id(bookId);
    }

    private void validatePrices(BigDecimal rentalPrice, BigDecimal coverPrice) {
        if (rentalPrice == null || coverPrice == null
                || rentalPrice.signum() < 0 || coverPrice.signum() < 0) {
            throw new IllegalArgumentException("rentalPrice and coverPrice must be non-negative");
        }
    }
}
