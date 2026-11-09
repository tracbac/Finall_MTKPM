package com.library.digital_library.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {
    @Size(min = 2, max = 50)
    private String username;

    @Email
    private String email;

    private String role;

    private String newPassword;
}

