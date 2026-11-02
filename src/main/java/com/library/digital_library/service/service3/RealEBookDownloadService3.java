package com.library.digital_library.service.service3;

import com.library.digital_library.model.Book;
import com.library.digital_library.model.model3.DownloadRequest3;
import com.library.digital_library.model.model3.DownloadResult3;
import com.library.digital_library.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * REAL SUBJECT (Proxy Pattern - Thành viên 3)
 * Đối tượng thực thi logic kỹ thuật tạo và cung cấp file tải E-Book thật sự
 * Không chứa logic kiểm soát bảo mật (Tuân thủ Single Responsibility Principle - SRP)
 */
@Service("realEBookDownloadService3")
@RequiredArgsConstructor
public class RealEBookDownloadService3 implements EBookDownloadService3 {

    private final BookRepository bookRepository;

    @Override
    public DownloadResult3 downloadEBook(DownloadRequest3 request) {
        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sách với ID: " + request.getBookId()));

        String sanitizedTitle = book.getTitle().replaceAll("[^a-zA-Z0-9.-]", "_");
        String fileName = sanitizedTitle + "_drm_protected.pdf";
        String secureDownloadToken = UUID.randomUUID().toString();
        String downloadUrl = "/api/v3/ebooks/stream/" + secureDownloadToken;
        long simulatedFileSize = 1024 * 1024 * 5; // 5 MB

        return DownloadResult3.builder()
                .success(true)
                .fileName(fileName)
                .fileFormat("PDF")
                .fileSize(simulatedFileSize)
                .downloadUrl(downloadUrl)
                .message("Tải tài liệu thành công. Nội dung đã được mã hóa bản quyền số.")
                .build();
    }
}
