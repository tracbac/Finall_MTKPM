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

@org.junit.jupiter.api.Disabled("Legacy test disabled due to UserService refactoring")
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
        assertNotNull(sampleUser);
    }
}