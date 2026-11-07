package com.library.digital_library.unittest;

import com.library.digital_library.controller.AuthController;
import com.library.digital_library.dto.AuthResponse;
import com.library.digital_library.dto.LoginRequest;
import com.library.digital_library.model.User;
import com.library.digital_library.repository.UserRepository;
import com.library.digital_library.security.JwtUtils;
import com.library.digital_library.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Kiểm thử chức năng đăng nhập")
class UserLoginTest {

    @Mock
    private AuthService authService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthController authController;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setId(1L);
        sampleUser.setUsername("tracbac");
        sampleUser.setPassword("encoded-password");
        sampleUser.setEmail("tracbac@library.com");
        sampleUser.setRole("USER");
    }

    @Test
    @DisplayName("Đăng nhập thành công với thông tin chính xác")
    void testLoginSuccess() {
        when(userRepository.findByUsername("tracbac")).thenReturn(Optional.of(sampleUser));
        when(jwtUtils.generateJwtToken("tracbac", "USER")).thenReturn("jwt-token");

        ResponseEntity<?> response = authController.login(new LoginRequest("tracbac", "123456"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        AuthResponse body = assertInstanceOf(AuthResponse.class, response.getBody());
        assertEquals("tracbac", body.getUsername());
        assertEquals("USER", body.getRole());
        assertEquals("jwt-token", body.getToken());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("Đăng nhập thất bại do sai mật khẩu")
    void testLoginWrongPassword() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        ResponseEntity<?> response = authController.login(new LoginRequest("tracbac", "wrongpassword"));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid username or password", response.getBody());
        verify(userRepository, never()).findByUsername("tracbac");
    }

    @Test
    @DisplayName("Đăng nhập thất bại do không tìm thấy tài khoản")
    void testLoginUserNotFound() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        ResponseEntity<?> response = authController.login(new LoginRequest("notfound", "123456"));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid username or password", response.getBody());
        verify(userRepository, never()).findByUsername("notfound");
    }
}
