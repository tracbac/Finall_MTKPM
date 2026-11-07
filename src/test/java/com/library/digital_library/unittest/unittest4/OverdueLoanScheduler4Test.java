package com.library.digital_library.unittest.unittest4;

import com.library.digital_library.model.model3.LoanRecord3;
import com.library.digital_library.service.service3.LoanService3;
import com.library.digital_library.service.service4.OverdueLoanScheduler4;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Kiểm thử rà soát quá hạn tự động hằng ngày")
class OverdueLoanScheduler4Test {

    @Mock
    private LoanService3 loanService;

    @Test
    @DisplayName("Lịch chạy hằng ngày gọi nghiệp vụ rà soát quá hạn của LoanService3")
    void dailyJobDelegatesToLoanService() {
        when(loanService.checkAndTriggerOverdueFines()).thenReturn(List.of(new LoanRecord3()));

        new OverdueLoanScheduler4(loanService).checkOverdueLoansDaily();

        verify(loanService, times(1)).checkAndTriggerOverdueFines();
    }
}
