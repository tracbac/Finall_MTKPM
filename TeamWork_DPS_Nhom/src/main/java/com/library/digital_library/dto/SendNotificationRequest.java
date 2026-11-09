package com.library.digital_library.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SendNotificationRequest(
        Long userId,
        @NotBlank @Size(max = 1000) String message) {
}
