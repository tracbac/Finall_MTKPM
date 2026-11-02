package com.library.digital_library.repository;

import com.library.digital_library.model.BookPrice;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookPriceRepository extends JpaRepository<BookPrice, Long> {
    Optional<BookPrice> findByBook_Id(Long bookId);

    List<BookPrice> findAllByBook_IdIn(Collection<Long> bookIds);

    void deleteByBook_Id(Long bookId);
}
