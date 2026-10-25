package com.library.digital_library.repository;

import com.library.digital_library.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    // Tìm kiếm sách theo tựa đề (hỗ trợ tìm gần đúng)
    List<Book> findByTitleContainingIgnoreCase(String title);
    
    // Tìm kiếm sách theo thể loại
    List<Book> findByCategory(String category);
}