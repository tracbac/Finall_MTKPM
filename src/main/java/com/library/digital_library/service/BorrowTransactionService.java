package com.library.digital_library.service;

import com.library.digital_library.dto.BorrowRequest;
import com.library.digital_library.model.Book;
import com.library.digital_library.model.BorrowTransaction;
import com.library.digital_library.model.User;
import com.library.digital_library.repository.BookRepository;
import com.library.digital_library.repository.BorrowTransactionRepository;
import com.library.digital_library.repository.UserRepository;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BorrowTransactionService {

    private static final String PENDING = "PENDING";
    private static final String BORROWED = "BORROWED";
    private static final String RETURNED = "RETURNED";
    private static final String CANCELED = "CANCELED";
    private static final String REJECTED = "REJECTED";
    private static final double DAILY_FINE_RATE = 5000.0;
    private static final int DEFAULT_RENEW_DAYS = 7;
    private static final int MAX_RENEWALS = 2;

    private final BorrowTransactionRepository transactionRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public BorrowTransactionService(
            BorrowTransactionRepository transactionRepository,
            BookRepository bookRepository,
            UserRepository userRepository,
            NotificationService notificationService) {
        this.transactionRepository = transactionRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public BorrowTransaction createBorrowRequest(String username, BorrowRequest request) {
        if (request == null || request.getBookId() == null || request.getBorrowDays() <= 0) {
            throw new IllegalArgumentException("bookId and positive borrowDays are required");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        Book book = bookRepository.findByIdForUpdate(request.getBookId())
                .orElseThrow(() -> new NoSuchElementException("Book not found"));
        if (book.getQuantity() <= 0) {
            throw new IllegalStateException("Book is out of stock");
        }

        book.setQuantity(book.getQuantity() - 1);
        BorrowTransaction transaction = new BorrowTransaction();
        transaction.setUser(user);
        transaction.setBook(book);
        transaction.setBorrowDate(LocalDate.now());
        transaction.setDueDate(LocalDate.now().plusDays(request.getBorrowDays()));
        transaction.setStatus(PENDING);
        transaction.setFineAmount(0.0);
        transaction.setRenewalCount(0);
        BorrowTransaction savedTransaction = transactionRepository.save(transaction);
        notificationService.notifyUser(user.getId(),
                "Yêu cầu mượn sách \"" + book.getTitle() + "\" đã được gửi và đang chờ duyệt.");
        notificationService.notifyRole("ADMIN",
                "Có yêu cầu mượn sách \"" + book.getTitle() + "\" mới từ " + user.getUsername() + ".");
        return savedTransaction;
    }

    @Transactional
    public BorrowTransaction approveTransaction(Long transactionId) {
        BorrowTransaction transaction = findTransaction(transactionId);
        requireStatus(transaction, PENDING, "Only pending requests can be approved");
        transaction.setStatus(BORROWED);
        BorrowTransaction savedTransaction = transactionRepository.save(transaction);
        notificationService.notifyUser(transaction.getUser().getId(),
                "Yêu cầu mượn sách \"" + transaction.getBook().getTitle() + "\" đã được duyệt.");
        return savedTransaction;
    }

    @Transactional
    public BorrowTransaction rejectBorrowRequest(Long transactionId) {
        BorrowTransaction transaction = findTransaction(transactionId);
        requireStatus(transaction, PENDING, "Only pending requests can be rejected");

        Book book = bookRepository.findByIdForUpdate(transaction.getBook().getId())
                .orElseThrow(() -> new NoSuchElementException("Book not found"));
        book.setQuantity(book.getQuantity() + 1);
        transaction.setStatus(REJECTED);
        BorrowTransaction savedTransaction = transactionRepository.save(transaction);
        notificationService.notifyUser(transaction.getUser().getId(),
                "Yêu cầu mượn sách \"" + transaction.getBook().getTitle() + "\" đã bị từ chối.");
        return savedTransaction;
    }

    @Transactional
    public BorrowTransaction returnBook(Long transactionId) {
        BorrowTransaction transaction = findTransaction(transactionId);
        requireStatus(transaction, BORROWED, "Only borrowed books can be returned");

        Book book = bookRepository.findByIdForUpdate(transaction.getBook().getId())
                .orElseThrow(() -> new NoSuchElementException("Book not found"));
        book.setQuantity(book.getQuantity() + 1);
        transaction.setStatus(RETURNED);
        LocalDate today = LocalDate.now();
        transaction.setReturnDate(today);
        double fineAmount = calculateFine(transaction.getDueDate(), today);
        transaction.setFineAmount(fineAmount);
        BorrowTransaction savedTransaction = transactionRepository.save(transaction);
        String message = fineAmount > 0
                ? "Sách \"" + transaction.getBook().getTitle() + "\" đã được trả trễ. Phí quá hạn: "
                        + fineAmount + " đ."
                : "Sách \"" + transaction.getBook().getTitle() + "\" đã được ghi nhận trả thành công.";
        notificationService.notifyUser(transaction.getUser().getId(), message);
        return savedTransaction;
    }

    @Transactional
    public BorrowTransaction renewBorrow(String username, Long transactionId) {
        BorrowTransaction transaction = findTransaction(transactionId);
        if (!transaction.getUser().getUsername().equals(username)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You can only renew your own borrow transaction");
        }
        return renewTransaction(transaction);
    }

    @Transactional
    public BorrowTransaction renewBorrowByAdmin(Long transactionId) {
        return renewTransaction(findTransaction(transactionId));
    }

    private BorrowTransaction renewTransaction(BorrowTransaction transaction) {
        requireStatus(transaction, BORROWED, "Only borrowed books can be renewed");
        LocalDate today = LocalDate.now();
        if (today.isAfter(transaction.getDueDate())) {
            throw new IllegalStateException("Sách đã quá hạn, không thể gia hạn.");
        }

        int renewalCount = transaction.getRenewalCount() == null ? 0 : transaction.getRenewalCount();
        if (renewalCount >= MAX_RENEWALS) {
            throw new IllegalStateException("Đã hết số lần gia hạn cho phép.");
        }

        transaction.setDueDate(transaction.getDueDate().plusDays(DEFAULT_RENEW_DAYS));
        transaction.setRenewalCount(renewalCount + 1);
        BorrowTransaction savedTransaction = transactionRepository.save(transaction);
        notificationService.notifyUser(transaction.getUser().getId(),
                "Đã gia hạn sách \"" + transaction.getBook().getTitle() + "\" đến ngày "
                        + transaction.getDueDate() + ".");
        return savedTransaction;
    }

    @Transactional
    public BorrowTransaction cancelBorrowRequest(String username, Long transactionId) {
        BorrowTransaction transaction = findTransaction(transactionId);
        if (!transaction.getUser().getUsername().equals(username)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You can only cancel your own borrow requests");
        }
        requireStatus(transaction, PENDING, "Only pending requests can be canceled");

        Book book = bookRepository.findByIdForUpdate(transaction.getBook().getId())
                .orElseThrow(() -> new NoSuchElementException("Book not found"));
        book.setQuantity(book.getQuantity() + 1);
        transaction.setStatus(CANCELED);
        return transactionRepository.save(transaction);
    }

    @Transactional(readOnly = true)
    public List<BorrowTransaction> getUserTransactions(Long userId) {
        return transactionRepository.findByUser_IdOrderByBorrowDateDesc(userId);
    }

    @Transactional(readOnly = true)
    public List<BorrowTransaction> getUserTransactionsByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        return getUserTransactions(user.getId());
    }

    @Transactional(readOnly = true)
    public List<BorrowTransaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    private BorrowTransaction findTransaction(Long transactionId) {
        return transactionRepository.findByIdForUpdate(transactionId)
                .orElseThrow(() -> new NoSuchElementException("Borrow transaction not found"));
    }

    private void requireStatus(BorrowTransaction transaction, String expected, String message) {
        if (!expected.equals(transaction.getStatus())) {
            throw new IllegalStateException(message);
        }
    }

    private double calculateFine(LocalDate dueDate, LocalDate returnDate) {
        if (dueDate == null || !returnDate.isAfter(dueDate)) {
            return 0.0;
        }
        return ChronoUnit.DAYS.between(dueDate, returnDate) * DAILY_FINE_RATE;
    }
}
