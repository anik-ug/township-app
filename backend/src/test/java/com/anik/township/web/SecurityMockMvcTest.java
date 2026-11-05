package com.anik.township.web;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.anik.township.auth.JwtAuthenticationFilter;
import com.anik.township.auth.JwtService;
import com.anik.township.auth.OtpService;
import com.anik.township.config.SecurityConfig;
import com.anik.township.domain.AppUser;
import com.anik.township.domain.Flat;
import com.anik.township.domain.Role;
import com.anik.township.repository.AppUserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {AuthController.class, PingController.class})
@Import({SecurityConfig.class, JwtService.class, JwtAuthenticationFilter.class})
@TestPropertySource(properties = {
        "app.jwt-secret=test-jwt-secret-that-is-at-least-32-chars",
        "app.cors-origins=http://localhost:5173"
})
class SecurityMockMvcTest {

    private static final String SECRET = "test-jwt-secret-that-is-at-least-32-chars";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JwtService jwtService;

    @MockBean
    AppUserRepository userRepository;

    @MockBean
    OtpService otpService;

    @Test
    void noTokenIsRejectedForEveryProtectedPrefix() throws Exception {
        mockMvc.perform(get("/api/resident/ping")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/helpdesk/ping")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/accounts/ping")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/committee/ping")).andExpect(status().isUnauthorized());
    }

    @Test
    void eachRoleCanReachOwnPingButNotAnotherRole() throws Exception {
        mockMvc.perform(get("/api/resident/ping").with(user("resident").roles("RESIDENT")))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"ok\":true,\"role\":\"RESIDENT\"}"));
        mockMvc.perform(get("/api/helpdesk/ping").with(user("helpdesk").roles("HELPDESK")))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/accounts/ping").with(user("accounts").roles("ACCOUNTS")))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/committee/ping").with(user("committee").roles("COMMITTEE")))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/accounts/ping").with(user("resident").roles("RESIDENT")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/committee/ping").with(user("helpdesk").roles("HELPDESK")))
                .andExpect(status().isForbidden());
    }

    @Test
    void tamperedAndExpiredTokensAreRejected() throws Exception {
        AppUser user = testUser(1L, Role.RESIDENT, 1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        String valid = jwtService.issueToken(user);
        String tampered = valid.substring(0, valid.length() - 1)
                + (valid.endsWith("a") ? "b" : "a");
        mockMvc.perform(get("/api/resident/ping").header("Authorization", "Bearer " + tampered))
                .andExpect(status().isUnauthorized());

        String expired = Jwts.builder()
                .subject(user.getPhone())
                .claim("userId", 1L)
                .claim("role", "RESIDENT")
                .claim("flatId", 1L)
                .expiration(Date.from(Instant.now().minusSeconds(1)))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();
        mockMvc.perform(get("/api/resident/ping").header("Authorization", "Bearer " + expired))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void knownAndUnknownOtpRequestsHaveTheSameResponse() throws Exception {
        String request = "{\"phone\":\"9000000001\"}";
        String unknownRequest = "{\"phone\":\"9000000099\"}";
        mockMvc.perform(post("/api/auth/request-otp").contentType("application/json").content(request))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"message\":\"OTP sent\"}"));
        mockMvc.perform(post("/api/auth/request-otp").contentType("application/json").content(unknownRequest))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"message\":\"OTP sent\"}"));
    }

    @Test
    void correctOtpReturnsTokenAndWrongAttemptsAreLimited() throws Exception {
        AppUser resident = testUser(1L, Role.RESIDENT, 1L);
        when(otpService.verify("9000000001", "123456")).thenReturn(resident);
        when(otpService.issueToken(resident)).thenReturn("signed-token");
        mockMvc.perform(post("/api/auth/verify-otp")
                        .contentType("application/json")
                        .content("{\"phone\":\"9000000001\",\"otp\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("signed-token"))
                .andExpect(jsonPath("$.role").value("RESIDENT"))
                .andExpect(jsonPath("$.flatNo").value("T07-1203"));

        when(otpService.verify("9000000001", "000000"))
                .thenThrow(new com.anik.township.auth.InvalidOtpException())
                .thenThrow(new com.anik.township.auth.InvalidOtpException())
                .thenThrow(new com.anik.township.auth.InvalidOtpException())
                .thenThrow(new com.anik.township.auth.InvalidOtpException())
                .thenThrow(new com.anik.township.auth.InvalidOtpException())
                .thenThrow(new com.anik.township.auth.OtpLockedException());
        for (int attempt = 0; attempt < 5; attempt++) {
            mockMvc.perform(post("/api/auth/verify-otp")
                            .contentType("application/json")
                            .content("{\"phone\":\"9000000001\",\"otp\":\"000000\"}"))
                    .andExpect(status().isUnauthorized());
        }
        mockMvc.perform(post("/api/auth/verify-otp")
                        .contentType("application/json")
                        .content("{\"phone\":\"9000000001\",\"otp\":\"000000\"}"))
                .andExpect(status().isTooManyRequests());
    }

    private static AppUser testUser(long id, Role role, long flatId) {
        AppUser user = new AppUser();
        user.setId(id);
        user.setPhone("9000000001");
        user.setName("Test User");
        user.setRole(role);
        Flat flat = new Flat();
        flat.setId(flatId);
        flat.setFlatNo("T07-1203");
        user.setFlat(flat);
        return user;
    }
}
