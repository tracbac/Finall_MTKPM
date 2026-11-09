package com.library.digital_library.service;

import com.library.digital_library.dto.EBookFileResponse3;
import com.library.digital_library.model.Book;
import com.library.digital_library.model.model3.EBookFile3;
import com.library.digital_library.repository.BookRepository;
import com.library.digital_library.repository.repository3.EBookFile3Repository;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class EBookFileService {

    private static final Logger logger = LoggerFactory.getLogger(EBookFileService.class);

    private final BookRepository bookRepository;
    private final EBookFile3Repository eBookFileRepository;
    private final SupabaseEBookStorage storage;

    public EBookFileService(
            BookRepository bookRepository,
            EBookFile3Repository eBookFileRepository,
            SupabaseEBookStorage storage) {
        this.bookRepository = bookRepository;
        this.eBookFileRepository = eBookFileRepository;
        this.storage = storage;
    }

    @Transactional
    public EBookFileResponse3 upload(Long bookId, MultipartFile file) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy sách."));
        String fileName = safeFileName(file == null ? null : file.getOriginalFilename(), book.getTitle());
        String storagePath = "ebook-" + bookId + ".pdf";
        try {
            storage.upload(storagePath, file);
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (EBookStorageException e) {
            logger.warn("Supabase Storage upload warning ({}). Storing file locally as fallback.", e.getMessage());
        }

        try {
            if (file != null && !file.isEmpty()) {
                java.nio.file.Path localDir = java.nio.file.Paths.get("uploads", "ebooks");
                java.nio.file.Files.createDirectories(localDir);
                java.nio.file.Path localFile = localDir.resolve(storagePath);
                java.nio.file.Files.write(localFile, file.getBytes());
            }
        } catch (Exception ex) {
            logger.warn("Could not save local copy of PDF: {}", ex.getMessage());
        }

        EBookFile3 eBookFile = eBookFileRepository.findByBookId(bookId)
                .orElseGet(() -> EBookFile3.builder().book(book).build());
        eBookFile.setStoragePath(storagePath);
        eBookFile.setFileName(fileName);
        eBookFile.setFileSize(file != null ? file.getSize() : 0L);
        eBookFile.setUpdatedAt(LocalDateTime.now());
        return EBookFileResponse3.from(eBookFileRepository.save(eBookFile));
    }

    @Transactional(readOnly = true)
    public EBookFile3 getForDownload(Long bookId) {
        return eBookFileRepository.findByBookId(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Sách này chưa được tải lên tệp PDF."));
    }

    @Transactional(readOnly = true)
    public boolean hasFile(Long bookId) {
        return bookId != null && eBookFileRepository.findByBookId(bookId).isPresent();
    }

    @Transactional
    public void registerSampleEBook(Book book) {
        if (book == null || book.getId() == null) return;
        if (eBookFileRepository.findByBookId(book.getId()).isPresent()) return;
        String safeName = safeFileName(null, book.getTitle());
        EBookFile3 file = EBookFile3.builder()
                .book(book)
                .fileName(safeName)
                .storagePath("samples/" + safeName)
                .fileSize(5 * 1024 * 1024L)
                .updatedAt(LocalDateTime.now())
                .build();
        eBookFileRepository.save(file);
    }

    @Transactional
    public void deleteForBook(Long bookId) {
        Optional<EBookFile3> file = eBookFileRepository.findByBookId(bookId);
        if (file.isEmpty()) {
            return;
        }
        String storagePath = file.get().getStoragePath();
        eBookFileRepository.delete(file.get());
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    storage.delete(storagePath);
                } catch (EBookStorageException exception) {
                    logger.error("Book {} was deleted but its PDF could not be removed from Supabase Storage",
                            bookId, exception);
                }
            }
        });
    }

    private String safeFileName(String originalName, String title) {
        String candidate = StringUtils.hasText(originalName) ? originalName : title + ".pdf";
        candidate = candidate.replace('\\', '/');
        candidate = candidate.substring(candidate.lastIndexOf('/') + 1)
                .replaceAll("[\\p{Cntrl}]", "")
                .trim();
        if (candidate.isBlank()) {
            candidate = "ebook.pdf";
        }
        if (!candidate.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            throw new IllegalArgumentException("Tên tệp E-Book phải có phần mở rộng .pdf.");
        }
        return candidate.length() > 255 ? candidate.substring(candidate.length() - 255) : candidate;
    }
}
