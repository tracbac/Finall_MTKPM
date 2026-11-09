package com.library.digital_library.controller;

import com.library.digital_library.dto.AuthRequest;
import com.library.digital_library.dto.UpdateUserRequest;
import com.library.digital_library.dto.UserDetailResponse;
import com.library.digital_library.dto.UserResponse;
import com.library.digital_library.model.BorrowTransaction;
import com.library.digital_library.model.User;
import com.library.digital_library.model.model3.LoanStatus3;
import com.library.digital_library.repository.BorrowTransactionRepository;
import com.library.digital_library.repository.NotificationRepository;
import com.library.digital_library.repository.UserRepository;
import com.library.digital_library.repository.repository3.DownloadLog3Repository;
import com.library.digital_library.repository.repository3.DrmLicense3Repository;
import com.library.digital_library.repository.repository3.EBookBorrowRequest3Repository;
import com.library.digital_library.repository.repository3.FineReceipt3Repository;
import com.library.digital_library.repository.repository3.LoanRecord3Repository;
import com.library.digital_library.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private BorrowTransactionRepository borrowTransactionRepository;

    @Autowired(required = false)
    private LoanRecord3Repository loanRecordRepository;

    @Autowired(required = false)
    private DrmLicense3Repository drmLicenseRepository;

    @Autowired(required = false)
    private EBookBorrowRequest3Repository ebookBorrowRequestRepository;

    @Autowired(required = false)
    private NotificationRepository notificationRepository;

    @Autowired(required = false)
    private FineReceipt3Repository fineReceiptRepository;

    @Autowired(required = false)
    private DownloadLog3Repository downloadLogRepository;

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(UserResponse.from(authService.createUserByAdmin(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDetailResponse> getUserDetail(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy tài khoản #" + id));

        long activeBorrows = borrowTransactionRepository.findByUser_IdOrderByBorrowDateDesc(id)
                .stream().filter(b -> "BORROWED".equalsIgnoreCase(b.getStatus())).count();
        long activeLoans = loanRecordRepository != null
                ? loanRecordRepository.countByUserIdAndStatus(id, LoanStatus3.BORROWED)
                + loanRecordRepository.countByUserIdAndStatus(id, LoanStatus3.EXTENDED)
                : 0;
        long totalBorrows = borrowTransactionRepository.findByUser_IdOrderByBorrowDateDesc(id).size();
        long activeDrmLicenses = drmLicenseRepository != null
                ? drmLicenseRepository.findByUserId(id).stream().filter(l -> "ACTIVE".equalsIgnoreCase(l.getStatus())).count()
                : 0;

        return ResponseEntity.ok(new UserDetailResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                activeBorrows + activeLoans,
                totalBorrows,
                activeDrmLicenses
        ));
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy tài khoản #" + id));

        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            String newUsername = request.getUsername().trim();
            userRepository.findByUsername(newUsername).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new IllegalArgumentException("Tên đăng nhập '" + newUsername + "' đã được sử dụng");
                }
            });
            user.setUsername(newUsername);
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            String newEmail = request.getEmail().trim();
            userRepository.findAll().stream()
                    .filter(u -> !u.getId().equals(id) && newEmail.equalsIgnoreCase(u.getEmail()))
                    .findFirst()
                    .ifPresent(existing -> {
                        throw new IllegalArgumentException("Email '" + newEmail + "' đã được sử dụng bởi tài khoản khác");
                    });
            user.setEmail(newEmail);
        }

        if (request.getRole() != null && !request.getRole().isBlank()) {
            String newRole = request.getRole().trim().toUpperCase();
            if (!Set.of("ADMIN", "USER").contains(newRole)) {
                throw new IllegalArgumentException("Vai trò phải là ADMIN hoặc USER");
            }
            user.setRole(newRole);
        }

        if (request.getNewPassword() != null && !request.getNewPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getNewPassword().trim()));
        }

        User saved = userRepository.save(user);
        return ResponseEntity.ok(UserResponse.from(saved));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Map<String, String>> deleteUser(
            @PathVariable Long id,
            Principal principal) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy tài khoản #" + id));

        if (principal != null && user.getUsername().equalsIgnoreCase(principal.getName())) {
            throw new IllegalArgumentException("Bạn không thể tự xóa tài khoản quản trị đang đăng nhập!");
        }

        List<BorrowTransaction> borrows = borrowTransactionRepository.findByUser_IdOrderByBorrowDateDesc(id);
        boolean hasActiveBorrows = borrows.stream().anyMatch(b ->
                "BORROWED".equalsIgnoreCase(b.getStatus()) || "PENDING".equalsIgnoreCase(b.getStatus()));
        if (hasActiveBorrows) {
            throw new IllegalStateException("Không thể xóa tài khoản đang có sách mượn tại quầy chưa trả hoặc đang chờ duyệt!");
        }

        if (loanRecordRepository != null) {
            long activeLoans = loanRecordRepository.countByUserIdAndStatus(id, LoanStatus3.BORROWED)
                    + loanRecordRepository.countByUserIdAndStatus(id, LoanStatus3.EXTENDED)
                    + loanRecordRepository.countByUserIdAndStatus(id, LoanStatus3.OVERDUE);
            if (activeLoans > 0) {
                throw new IllegalStateException("Không thể xóa tài khoản đang có giao dịch mượn sách chưa trả hoặc quá hạn!");
            }
        }

        // Dọn dẹp các bản ghi liên kết lịch sử
        borrowTransactionRepository.deleteAll(borrows);
        if (notificationRepository != null) {
            notificationRepository.deleteAll(notificationRepository.findByUserId(id));
        }
        if (drmLicenseRepository != null) {
            drmLicenseRepository.deleteAll(drmLicenseRepository.findByUserId(id));
        }
        if (ebookBorrowRequestRepository != null) {
            ebookBorrowRequestRepository.deleteAll(ebookBorrowRequestRepository.findAllByUserIdOrderByRequestedAtDesc(id));
        }
        if (loanRecordRepository != null) {
            loanRecordRepository.deleteAll(loanRecordRepository.findByUserId(id));
        }
        if (fineReceiptRepository != null) {
            fineReceiptRepository.deleteAll(fineReceiptRepository.findByUserId(id));
        }
        if (downloadLogRepository != null) {
            downloadLogRepository.deleteAll(downloadLogRepository.findByUserIdOrderByRequestedAtDesc(id));
        }

        userRepository.delete(user);
        return ResponseEntity.ok(Map.of("message", "Đã xóa tài khoản " + user.getUsername() + " thành công."));
    }
}
