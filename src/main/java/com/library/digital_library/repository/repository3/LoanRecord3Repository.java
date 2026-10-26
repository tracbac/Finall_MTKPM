package com.library.digital_library.repository.repository3;

import com.library.digital_library.model.model3.LoanRecord3;
import com.library.digital_library.model.model3.LoanStatus3;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Repository
public interface LoanRecord3Repository extends JpaRepository<LoanRecord3, Long> {
    List<LoanRecord3> findByUserId(Long userId);
    List<LoanRecord3> findByBookId(Long bookId);
    List<LoanRecord3> findByStatus(LoanStatus3 status);
    List<LoanRecord3> findByUserIdAndStatus(Long userId, LoanStatus3 status);
    List<LoanRecord3> findByDueDateBeforeAndStatusIn(LocalDate date, Collection<LoanStatus3> statuses);
    long countByUserIdAndStatus(Long userId, LoanStatus3 status);
}
