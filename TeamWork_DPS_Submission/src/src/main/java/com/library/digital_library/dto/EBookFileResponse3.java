package com.library.digital_library.dto;

import com.library.digital_library.model.model3.EBookFile3;
import java.time.LocalDateTime;

public record EBookFileResponse3(
        Long bookId,
        String fileName,
        Long fileSize,
        LocalDateTime updatedAt) {

    public static EBookFileResponse3 from(EBookFile3 file) {
        return new EBookFileResponse3(
                file.getBook().getId(),
                file.getFileName(),
                file.getFileSize(),
                file.getUpdatedAt());
    }
}
