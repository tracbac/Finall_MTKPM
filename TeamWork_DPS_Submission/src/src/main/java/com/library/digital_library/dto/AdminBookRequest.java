package com.library.digital_library.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

@Data
public class AdminBookRequest {
    @NotBlank
    @Size(max = 255)
    private String title;

    @NotBlank
    @Size(max = 255)
    private String author;

    @Size(max = 100)
    private String category;

    @NotNull
    @PositiveOrZero
    private Integer quantity;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal coverPrice;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal rentalPrice;

    private MultipartFile imageFile;
}