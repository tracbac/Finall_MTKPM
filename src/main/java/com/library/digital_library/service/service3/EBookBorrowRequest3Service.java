package com.library.digital_library.service.service3;

import com.library.digital_library.model.Book;
import com.library.digital_library.model.User;
import com.library.digital_library.model.model3.DrmLicense3;
import com.library.digital_library.model.model3.EBookBorrowRequest3;
import com.library.digital_library.model.model3.EBookRequestStatus3;
import com.library.digital_library.model.model3.IssueDrmRequest3;
import com.library.digital_library.repository.BookRepository;
import com.library.digital_library.repository.UserRepository;
import com.library.digital_library.repository.repository3.DrmLicense3Repository;
import com.library.digital_library.repository.repository3.EBookBorrowRequest3Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EBookBorrowRequest3Service {

    private final EBookBorrowRequest3Repository requestRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final DrmLicense3Repository drmLicenseRepository;
    private final DrmLicenseService3 drmLicenseService;

    @Transactional
    public EBookBorrowRequest3 createRequest(String username, Long bookId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy tài khoản người dùng."));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy sách được yêu cầu."));

        if (requestRepository.existsByUserIdAndBookIdAndStatus(
                user.getId(), bookId, EBookRequestStatus3.PENDING)) {
            throw new IllegalStateException("Bạn đã có yêu cầu đang chờ duyệt cho sách này.");
        }
        if (drmLicenseRepository.findFirstByUserIdAndBookIdAndStatus(user.getId(), bookId, "ACTIVE").isPresent()) {
            throw new IllegalStateException("Bạn đã có giấy phép DRM còn hiệu lực cho sách này.");
        }

        return requestRepository.save(EBookBorrowRequest3.builder()
                .user(user)
                .book(book)
                .status(EBookRequestStatus3.PENDING)
                .requestedAt(LocalDateTime.now())
                .build());
    }

    @Transactional(readOnly = true)
    public List<EBookBorrowRequest3> getRequestsForUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy tài khoản người dùng."));
        return requestRepository.findAllByUserIdOrderByRequestedAtDesc(user.getId());
    }

    @Transactional
    public EBookBorrowRequest3 cancelRequest(String username, Long requestId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy tài khoản người dùng."));
        EBookBorrowRequest3 request = getRequest(requestId);
        if (!request.getUser().getId().equals(user.getId())) {
            throw new NoSuchElementException("Không tìm thấy yêu cầu E-Book.");
        }
        requirePending(request);
        request.setStatus(EBookRequestStatus3.CANCELED);
        request.setReviewedAt(LocalDateTime.now());
        request.setReviewNote("Đã hủy bởi người dùng.");
        return requestRepository.save(request);
    }

    @Transactional(readOnly = true)
    public List<EBookBorrowRequest3> getAllRequests() {
        return requestRepository.findAllByOrderByRequestedAtDesc();
    }

    @Transactional
    public EBookBorrowRequest3 approveRequest(Long requestId, Integer validDays, Integer maxDownloads) {
        EBookBorrowRequest3 request = getRequest(requestId);
        requirePending(request);
        if (validDays == null || validDays < 1 || maxDownloads == null || maxDownloads < 1) {
            throw new IllegalArgumentException("Thời hạn và số lượt tải phải lớn hơn 0.");
        }

        IssueDrmRequest3 issueRequest = new IssueDrmRequest3();
        issueRequest.setUserId(request.getUser().getId());
        issueRequest.setBookId(request.getBook().getId());
        issueRequest.setValidDays(validDays);
        issueRequest.setMaxDownloads(maxDownloads);
        DrmLicense3 license = drmLicenseService.issueLicense(issueRequest);

        request.setStatus(EBookRequestStatus3.APPROVED);
        request.setReviewedAt(LocalDateTime.now());
        request.setReviewNote(String.format(
                "Đã duyệt: hiệu lực %d ngày, tối đa %d lượt tải.", validDays, maxDownloads));
        request.setLicenseKey(license.getLicenseKey());
        return requestRepository.save(request);
    }

    @Transactional
    public EBookBorrowRequest3 rejectRequest(Long requestId, String note) {
        EBookBorrowRequest3 request = getRequest(requestId);
        requirePending(request);
        request.setStatus(EBookRequestStatus3.REJECTED);
        request.setReviewedAt(LocalDateTime.now());
        request.setReviewNote(note == null || note.isBlank() ? "Yêu cầu đã bị từ chối." : note.trim());
        return requestRepository.save(request);
    }

    private EBookBorrowRequest3 getRequest(Long requestId) {
        return requestRepository.findByIdForUpdate(requestId)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy yêu cầu E-Book."));
    }

    private void requirePending(EBookBorrowRequest3 request) {
        if (request.getStatus() != EBookRequestStatus3.PENDING) {
            throw new IllegalStateException("Yêu cầu này đã được xử lý trước đó.");
        }
    }
}
