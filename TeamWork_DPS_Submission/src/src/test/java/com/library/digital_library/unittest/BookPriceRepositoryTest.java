package com.library.digital_library.unittest;

import com.library.digital_library.model.Book;
import com.library.digital_library.model.BookPrice;
import com.library.digital_library.repository.BookPriceRepository;
import com.library.digital_library.repository.BookRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class BookPriceRepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookPriceRepository bookPriceRepository;

    @Test
    void persistsRentalAndCoverPricesAndFiltersCatalog() {
        Book book = new Book();
        book.setTitle("Spring Boot");
        book.setAuthor("Author");
        book.setCategory("Technology");
        book.setQuantity(5);
        book = bookRepository.save(book);

        BookPrice price = new BookPrice();
        price.setBook(book);
        price.setRentalPrice(new BigDecimal("2.50"));
        price.setCoverPrice(new BigDecimal("20.00"));
        bookPriceRepository.save(price);

        assertEquals(new BigDecimal("2.50"),
                bookPriceRepository.findByBook_Id(book.getId()).orElseThrow().getRentalPrice());
        assertEquals(List.of(book.getId()),
                bookRepository.search("spring", "tech").stream().map(Book::getId).toList());
        assertEquals(List.of(), bookRepository.search("missing", ""));
        assertEquals(List.of(book.getId()),
                bookRepository.search("", "").stream().map(Book::getId).toList());
    }
}
