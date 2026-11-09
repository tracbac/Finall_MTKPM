package com.library.digital_library.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Data
public class BorrowRequest {
    @NotNull
    @Positive
    private Long bookId;

    @Positive
    @JsonAlias({"rentalDays", "days"})
    private int borrowDays; // Số ngày mượn
}