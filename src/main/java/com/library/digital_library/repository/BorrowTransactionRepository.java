package com.library.digital_library.repository;

import com.library.digital_library.model.BorrowTransaction;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BorrowTransactionRepository extends JpaRepository<BorrowTransaction, Long> {
    List<BorrowTransaction> findByUser_IdOrderByBorrowDateDesc(Long userId);

    // Dùng cho nhắc hạn trả sách tự động (Observer Pattern - Thành viên 4)
    List<BorrowTransaction> findByStatusAndDueDate(String status, LocalDate dueDate);

    long countByUser_IdAndStatusAndDueDateBefore(Long userId, String status, LocalDate date);

    /**
     * Số yêu cầu mượn trực tuyến (luồng BorrowTransaction) mà độc giả đang giữ quá hạn:
     * trạng thái BORROWED và hạn trả đã qua. Dùng để khóa quyền mượn/tải giống phiếu mượn OVERDUE.
     */
    default long countOverdueBorrows(Long userId, LocalDate today) {
        return countByUser_IdAndStatusAndDueDateBefore(userId, "BORROWED", today);
    }

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select transaction from BorrowTransaction transaction where transaction.id = :id")
    Optional<BorrowTransaction> findByIdForUpdate(@Param("id") Long id);
}