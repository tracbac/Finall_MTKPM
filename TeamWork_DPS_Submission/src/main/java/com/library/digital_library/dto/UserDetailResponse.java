package com.library.digital_library.dto;

public record UserDetailResponse(
        Long id,
        String username,
        String email,
        String role,
        long activeLoans,
        long totalBorrows,
        long activeDrmLicenses
) {}

