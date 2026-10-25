package com.library.digital_library.unittest;

import com.library.digital_library.model.User;
import com.library.digital_library.repository.UserRepository;
import com.library.digital_library.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Kiểm thử chức năng Đăng nhập (User Login Unit Test)")
class UserLoginTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setId(1L);
        sampleUser.setUsername("tracbac");
        sampleUser.setPassword("123456");
        sampleUser.setEmail("tracbac@library.com");
        sampleUser.setRole("MEMBER");
    }

    @Test
    @DisplayName("Test Đăng nhập thành công với thông tin chính xác")
    void testLogin_Success() {
        when(userRepository.findByUsername("tracbac")).thenReturn(Optional.of(sampleUser));

        User loggedInUser = userService.login("tracbac", "123456");

        assertNotNull(loggedInUser);
        assertEquals("tracbac", loggedInUser.getUsername());
    }

    @Test
    @DisplayName("Test Đăng nhập thất bại do sai mật khẩu")
    void testLogin_WrongPassword_ThrowsException() {
        when(userRepository.findByUsername("tracbac")).thenReturn(Optional.of(sampleUser));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.login("tracbac", "wrongpassword");
        });

        assertEquals("Mật khẩu không chính xác!", exception.getMessage());
    }

    @Test
    @DisplayName("Test Đăng nhập thất bại do không tìm thấy tài khoản")
    void testLogin_UserNotFound_ThrowsException() {
        when(userRepository.findByUsername("notfound")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.login("notfound", "123456");
        });

        assertEquals("Không tìm thấy tài khoản người dùng!", exception.getMessage());
    }
}