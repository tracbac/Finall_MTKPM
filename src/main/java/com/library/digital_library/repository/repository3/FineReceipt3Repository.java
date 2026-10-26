package com.library.digital_library.repository.repository3;

import com.library.digital_library.model.model3.FineReceipt3;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FineReceipt3Repository extends JpaRepository<FineReceipt3, Long> {
    List<FineReceipt3> findByUserId(Long userId);
    List<FineReceipt3> findByUserIdAndIsPaidFalse(Long userId);
    List<FineReceipt3> findByLoanRecordId(Long loanRecordId);
    long countByUserIdAndIsPaidFalse(Long userId);
}
