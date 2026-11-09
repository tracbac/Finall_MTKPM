package com.library.digital_library.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record EBookApprovalSettings3(
        @NotNull @Min(1) Integer validDays,
        @NotNull @Min(1) Integer maxDownloads) {
}
