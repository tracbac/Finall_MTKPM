package com.library.digital_library.service;

import com.library.digital_library.model.Book;
import com.library.digital_library.model.BorrowTransaction;
import com.library.digital_library.model.User;
import com.library.digital_library.repository.BookRepository;
import com.library.digital_library.repository.BorrowTransactionRepository;
import com.library.digital_library.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class BorrowTransactionService {

    @Autowired
    private BorrowTransactionRepository transactionRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    public List<BorrowTransaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    public BorrowTransaction borrowBook(Long userId, Long bookId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sách!"));

        if (book.getQuantity() <= 0) {
            throw new RuntimeException("Sách đã hết trong kho!");
        }

        // Giảm số lượng sách trong kho
        book.setQuantity(book.getQuantity() - 1);
        bookRepository.save(book);

        // Tạo giao dịch mượn
        BorrowTransaction transaction = new BorrowTransaction();
        transaction.setUser(user);
        transaction.setBook(book);
        transaction.setBorrowDate(LocalDate.now());
        transaction.setDueDate(LocalDate.now().plusDays(14)); // Mượn trong 14 ngày
        transaction.setStatus("BORROWED");

        return transactionRepository.save(transaction);
    }
}