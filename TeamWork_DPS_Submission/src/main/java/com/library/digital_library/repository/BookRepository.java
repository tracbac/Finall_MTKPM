package com.library.digital_library.repository;

import com.library.digital_library.model.Book;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    @Query("""
            select b from Book b
            where (:title = '' or lower(b.title) like lower(concat('%', :title, '%')))
              and (:category = '' or lower(b.category) like lower(concat('%', :category, '%')))
            order by b.title
            """)
    List<Book> search(@Param("title") String title, @Param("category") String category);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Book b where b.id = :id")
    Optional<Book> findByIdForUpdate(@Param("id") Long id);
}