package com.library.digital_library.unittest.unittest3;

import com.library.digital_library.model.User;
import com.library.digital_library.repository.UserRepository;
import com.library.digital_library.security.JwtUtils;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Kiểm thử phân quyền API mượn/phạt: chỉ thủ thư (ADMIN) được xác nhận nộp phạt,
 * rà soát quá hạn, cho mượn tại quầy và xem toàn bộ phiếu mượn.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Kiểm thử phân quyền API mượn sách & nộp phạt")
class LoanApiSecurity3Test {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtils jwtUtils;

    private User reader;
    private String readerToken;
    private String adminToken;

    @BeforeEach
    void setUp() {
        reader = createUser("USER");
        readerToken = "Bearer " + jwtUtils.generateJwtToken(reader.getUsername(), "USER");
        User admin = createUser("ADMIN");
        adminToken = "Bearer " + jwtUtils.generateJwtToken(admin.getUsername(), "ADMIN");
    }

    @Test
    @DisplayName("Độc giả KHÔNG được tự xác nhận đã nộp phạt")
    void readerCannotConfirmFinePayment() throws Exception {
        mockMvc.perform(post("/api/v3/loans/fines/1/pay")
                        .header("Authorization", readerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"paymentMethod\":\"CASH\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Độc giả KHÔNG được rà soát quá hạn hay xem toàn bộ phiếu mượn")
    void readerCannotUseLibrarianLoanApis() throws Exception {
        mockMvc.perform(post("/api/v3/loans/check-overdue").header("Authorization", readerToken))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v3/loans/all").header("Authorization", readerToken))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/v3/loans/1/return").header("Authorization", readerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Thủ thư được gọi API xác nhận nộp phạt (biên lai không tồn tại -> 404, không phải 403)")
    void adminCanCallConfirmFinePayment() throws Exception {
        mockMvc.perform(post("/api/v3/loans/fines/999999/pay")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"paymentMethod\":\"CASH\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Độc giả vẫn xem được khoản phạt của mình")
    void readerCanStillViewOwnFines() throws Exception {
        mockMvc.perform(get("/api/v3/loans/fines/user/" + reader.getId()).header("Authorization", readerToken))
                .andExpect(status().isOk());
    }

    private User createUser(String role) {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        User user = new User();
        user.setUsername(role.toLowerCase() + "-sec-" + suffix);
        user.setEmail(role.toLowerCase() + "-sec-" + suffix + "@library.test");
        user.setPassword("not-used-in-jwt-tests");
        user.setRole(role);
        return userRepository.save(user);
    }
}
