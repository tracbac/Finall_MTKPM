package com.library.digital_library.service.service3;

import com.library.digital_library.model.Book;
import com.library.digital_library.model.model3.DownloadRequest3;
import com.library.digital_library.model.model3.DownloadResult3;
import com.library.digital_library.model.model3.EBookFile3;
import com.library.digital_library.repository.BookRepository;
import com.library.digital_library.repository.repository3.DrmLicense3Repository;
import com.library.digital_library.service.EBookFileService;
import com.library.digital_library.service.SupabaseEBookStorage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * REAL SUBJECT (Proxy Pattern - Thành viên 3)
 * Đối tượng thực thi logic kỹ thuật tạo và cung cấp file tải E-Book thật sự
 * Hỗ trợ Fallback tự động sinh file PDF chuẩn có con dấu bản quyền Watermark DRM
 * khi chưa có tệp lưu trên Supabase Storage hoặc chạy môi trường nội bộ/offline.
 */
@Service("realEBookDownloadService3")
@RequiredArgsConstructor
public class RealEBookDownloadService3 implements EBookDownloadService3 {

    private static final Logger log = LoggerFactory.getLogger(RealEBookDownloadService3.class);

    private final EBookFileService eBookFileService;
    private final SupabaseEBookStorage storage;
    private final BookRepository bookRepository;
    private final DrmLicense3Repository drmLicenseRepository;

    @Override
    public DownloadResult3 downloadEBook(DownloadRequest3 request) {
        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sách với ID: " + request.getBookId()));

        EBookFile3 file = null;
        try {
            file = eBookFileService.getForDownload(request.getBookId());
        } catch (Exception e) {
            log.info("[Fallback PDF] Sách #{} chưa có file PDF riêng trong DB, kích hoạt Fallback.", request.getBookId());
        }

        byte[] fileContent = null;
        if (file != null && file.getStoragePath() != null) {
            try {
                java.nio.file.Path localFile = java.nio.file.Paths.get("uploads", "ebooks", file.getStoragePath());
                if (java.nio.file.Files.exists(localFile)) {
                    fileContent = java.nio.file.Files.readAllBytes(localFile);
                }
            } catch (Exception ignored) {}

            if (fileContent == null || fileContent.length == 0) {
                try {
                    fileContent = storage.download(file.getStoragePath());
                } catch (Exception e) {
                    log.warn("[Fallback PDF] Không thể tải file từ Supabase Storage ({}), chuyển sang Fallback PDF.", e.getMessage());
                }
            }
        }

        // Cơ chế Fallback thông minh: Tự sinh tệp PDF thật chuẩn có DRM Watermark
        if (fileContent == null || fileContent.length == 0) {
            String watermark = drmLicenseRepository.findByLicenseKey(request.getLicenseKey())
                    .map(l -> l.getWatermark())
                    .orElse("Bản quyền số cấp cho User ID: " + request.getUserId() + " | Mã GP: " + request.getLicenseKey());
            fileContent = generateFallbackPdf(book, watermark);
        }

        String fileName = (file != null && file.getFileName() != null)
                ? file.getFileName()
                : sanitizeFileName(book.getTitle()) + ".pdf";

        return DownloadResult3.builder()
                .success(true)
                .fileName(fileName)
                .fileFormat("PDF")
                .fileSize((long) fileContent.length)
                .fileContent(fileContent)
                .message("Giấy phép hợp lệ. Tệp PDF đã được tải xuống thành công.")
                .build();
    }

    private byte[] generateFallbackPdf(Book book, String watermark) {
        String title = sanitizeAscii(book.getTitle() != null ? book.getTitle() : "E-Book");
        String author = sanitizeAscii(book.getAuthor() != null ? book.getAuthor() : "Tac gia");
        String safeWatermark = sanitizeAscii(watermark);
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

        String contentStream = "BT\n"
                + "/F1 20 Tf\n"
                + "50 780 Td\n"
                + "(" + escapePdfText(title) + ") Tj\n"
                + "/F1 13 Tf\n"
                + "0 -35 Td\n"
                + "(Tac gia: " + escapePdfText(author) + ") Tj\n"
                + "/F1 11 Tf\n"
                + "0 -35 Td\n"
                + "(Thu vien Moc - An ban E-Book dien tu co ban quyen) Tj\n"
                + "/F1 9 Tf\n"
                + "0 -30 Td\n"
                + "(DRM WATERMARK: " + escapePdfText(safeWatermark) + ") Tj\n"
                + "0 -25 Td\n"
                + "(Thoi diem tai: " + now + ") Tj\n"
                + "/F1 11 Tf\n"
                + "0 -50 Td\n"
                + "(Noi dung sach dien tu da duoc dong dau chim bao ve ban quyen DRM.) Tj\n"
                + "0 -25 Td\n"
                + "(Cam sao chep, phat tan hoac su dung trai phep.) Tj\n"
                + "ET\n";

        byte[] streamBytes = contentStream.getBytes(StandardCharsets.US_ASCII);

        StringBuilder sb = new StringBuilder();
        sb.append("%PDF-1.4\n");

        int[] offsets = new int[5];
        offsets[0] = sb.length();
        sb.append("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");

        offsets[1] = sb.length();
        sb.append("2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n");

        offsets[2] = sb.length();
        sb.append("3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >>\nendobj\n");

        offsets[3] = sb.length();
        sb.append("4 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n");

        offsets[4] = sb.length();
        sb.append("5 0 obj\n<< /Length ").append(streamBytes.length).append(" >>\nstream\n");
        sb.append(contentStream);
        sb.append("endstream\nendobj\n");

        int startXref = sb.length();
        sb.append("xref\n0 6\n");
        sb.append("0000000000 65535 f \n");
        for (int offset : offsets) {
            sb.append(String.format("%010d 00000 n \n", offset));
        }
        sb.append("trailer\n<< /Size 6 /Root 1 0 R >>\n");
        sb.append("startxref\n").append(startXref).append("\n%%EOF\n");

        return sb.toString().getBytes(StandardCharsets.US_ASCII);
    }

    private String sanitizeAscii(String text) {
        if (text == null) return "";
        String nfd = java.text.Normalizer.normalize(text, java.text.Normalizer.Form.NFD);
        String withoutDiacritics = nfd.replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replace("đ", "d").replace("Đ", "D");
        return withoutDiacritics.replaceAll("[^\\x20-\\x7E]", " ");
    }

    private String escapePdfText(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }

    private String sanitizeFileName(String title) {
        if (title == null || title.isBlank()) return "ebook";
        return sanitizeAscii(title).toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-+|-+$", "");
    }
}
