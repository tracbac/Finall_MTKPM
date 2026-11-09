package com.library.digital_library.repository.repository3;

import com.library.digital_library.model.model3.EBookBorrowRequest3;
import com.library.digital_library.model.model3.EBookRequestStatus3;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EBookBorrowRequest3Repository extends JpaRepository<EBookBorrowRequest3, Long> {

    List<EBookBorrowRequest3> findAllByUserIdOrderByRequestedAtDesc(Long userId);

    List<EBookBorrowRequest3> findAllByOrderByRequestedAtDesc();

    boolean existsByUserIdAndBookIdAndStatus(Long userId, Long bookId, EBookRequestStatus3 status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select request from EBookBorrowRequest3 request where request.id = :id")
    Optional<EBookBorrowRequest3> findByIdForUpdate(@Param("id") Long id);
}
