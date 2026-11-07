package com.library.digital_library.model.model3;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DownloadRequest3 {
    private Long userId;
    @NotNull
    private Long bookId;
    @NotBlank
    private String licenseKey;
    private String clientIp;
}
