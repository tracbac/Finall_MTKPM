package com.library.digital_library.service.service4;

import com.library.digital_library.model.model3.LoanRecord3;
import com.library.digital_library.service.service3.LoanService3;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Tự động rà soát phiếu mượn quá hạn mỗi ngày (Thành viên 4).
 *
 * Trước đây việc chuyển phiếu sang OVERDUE chỉ xảy ra khi Admin bấm "Rà soát quá hạn";
 * nếu Admin quên, độc giả giữ sách quá hạn vẫn mượn/tải E-Book được.
 * Lớp này chỉ lo LỊCH CHẠY; nghiệp vụ rà soát vẫn nằm trong LoanService3 (SRP).
 * LoanService3 phát sự kiện LOAN_OVERDUE nên độc giả tự nhận thông báo qua Observer.
 */
@Component
public class OverdueLoanScheduler4 {

    private static final Logger log = LoggerFactory.getLogger(OverdueLoanScheduler4.class);

    private final LoanService3 loanService;

    public OverdueLoanScheduler4(LoanService3 loanService) {
        this.loanService = loanService;
    }

    /** Chạy lúc 00:05 mỗi ngày (giờ Việt Nam); đổi lịch qua app.loans.overdue-check-cron. */
    @Scheduled(cron = "${app.loans.overdue-check-cron:0 5 0 * * *}", zone = "Asia/Ho_Chi_Minh")
    public void checkOverdueLoansDaily() {
        List<LoanRecord3> updated = loanService.checkAndTriggerOverdueFines();
        log.info("[Scheduler] Rà soát quá hạn tự động: {} phiếu mượn chuyển sang OVERDUE.", updated.size());
    }
}
