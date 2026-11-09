package com.library.digital_library.config;

import com.library.digital_library.security.CustomUserDetailsService;
import com.library.digital_library.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.core.annotation.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtAuthenticationFilterRegistration(
            JwtAuthenticationFilter filter) {
        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    @Order(1)
    public SecurityFilterChain publicBookCatalogFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/api/books", "/api/books/**")
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                .accessDeniedHandler((request, response, exception) -> {
                    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                    logger.warn("Access denied for {} {}. Authorities: {}",
                            request.getMethod(),
                            request.getRequestURI(),
                            authentication == null ? "none" : authentication.getAuthorities());
                    response.sendError(HttpServletResponse.SC_FORBIDDEN);
                }))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/index.html", "/books.html", "/about.html", "/auth.html", "/admin.html", "/my-books.html", "/favicon.ico").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/notifications/admin", "/api/notifications/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v3/drm/issue", "/api/v3/drm/revoke").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/v3/drm/user/**", "/api/v3/drm/license/**",
                        "/api/v3/ebooks/logs/user/**").hasRole("ADMIN")
                // Nghiệp vụ của thủ thư: cho mượn tại quầy, xác nhận trả sách, rà soát quá hạn, xác nhận đã nộp phạt.
                // Độc giả chỉ được xem phiếu mượn/khoản phạt của mình và gia hạn.
                .requestMatchers(HttpMethod.POST, "/api/v3/loans/borrow", "/api/v3/loans/check-overdue",
                        "/api/v3/loans/*/return", "/api/v3/loans/fines/*/pay").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/v3/loans/all", "/api/v3/loans/overdue").hasRole("ADMIN")
                .requestMatchers("/api/borrows/**").authenticated()
                .anyRequest().authenticated()
            );

        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}