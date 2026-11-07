package com.library.digital_library.service.service3;

import com.library.digital_library.model.model3.DownloadRequest3;
import com.library.digital_library.model.model3.DownloadResult3;

/**
 * SUBJECT INTERFACE (Proxy Pattern - Thành viên 3)
 * Giao diện định nghĩa nghiệp vụ tải tài liệu / E-Book
 */
public interface EBookDownloadService3 {

    /**
     * Tải E-Book có kiểm soát bản quyền số
     *
     * @param request Thông tin yêu cầu tải (userId, bookId, licenseKey, clientIp)
     * @return Kết quả tải tài liệu kèm thông tin mã hóa bản quyền
     */
    DownloadResult3 downloadEBook(DownloadRequest3 request);
}
