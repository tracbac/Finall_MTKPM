package com.library.digital_library.service.service3;

import com.library.digital_library.model.model3.DownloadRequest3;
import com.library.digital_library.model.model3.DownloadResult3;
import com.library.digital_library.model.model3.EBookFile3;
import com.library.digital_library.service.EBookFileService;
import com.library.digital_library.service.SupabaseEBookStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * REAL SUBJECT (Proxy Pattern - Thành viên 3)
 * Đối tượng thực thi logic kỹ thuật tạo và cung cấp file tải E-Book thật sự
 * Không chứa logic kiểm soát bảo mật (Tuân thủ Single Responsibility Principle - SRP)
 */
@Service("realEBookDownloadService3")
@RequiredArgsConstructor
public class RealEBookDownloadService3 implements EBookDownloadService3 {

    private final EBookFileService eBookFileService;
    private final SupabaseEBookStorage storage;

    @Override
    public DownloadResult3 downloadEBook(DownloadRequest3 request) {
        EBookFile3 file = eBookFileService.getForDownload(request.getBookId());
        byte[] fileContent = storage.download(file.getStoragePath());

        return DownloadResult3.builder()
                .success(true)
                .fileName(file.getFileName())
                .fileFormat("PDF")
                .fileSize(file.getFileSize())
                .fileContent(fileContent)
                .message("Giấy phép hợp lệ. Tệp PDF đã được tải xuống.")
                .build();
    }
}
