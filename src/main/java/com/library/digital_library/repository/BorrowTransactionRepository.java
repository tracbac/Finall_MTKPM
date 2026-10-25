package com.library.digital_library.repository;

import com.library.digital_library.model.BorrowTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BorrowTransactionRepository extends JpaRepository<BorrowTransaction, Long> {
    // Lấy danh sách phiếu mượn theo User ID
    List<BorrowTransaction> findByUserId(Long userId);
    
    // Lấy danh sách theo trạng thái
    List<BorrowTransaction> findByStatus(String status);
}