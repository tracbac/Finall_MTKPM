package com.library.digital_library.model.model3;

import lombok.Data;

@Data
public class DownloadRequest3 {
    private Long userId;
    private Long bookId;
    private String licenseKey;
    private String clientIp;
}
