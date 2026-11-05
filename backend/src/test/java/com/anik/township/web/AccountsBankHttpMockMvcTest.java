package com.anik.township.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.anik.township.accounts.BankStatementService;
import com.anik.township.auth.JwtAuthenticationFilter;
import com.anik.township.auth.JwtService;
import com.anik.township.config.SecurityConfig;
import com.anik.township.domain.AppUser;
import com.anik.township.domain.BankRowStatus;
import com.anik.township.domain.Role;
import com.anik.township.repository.AppUserRepository;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.http.HttpStatus;

@WebMvcTest(AccountsBankController.class)
@Import({SecurityConfig.class, JwtService.class, JwtAuthenticationFilter.class, GlobalExceptionHandler.class})
@TestPropertySource(properties = {
        "app.jwt-secret=test-jwt-secret-that-is-at-least-32-chars",
        "app.cors-origins=http://localhost:5173"
})
class AccountsBankHttpMockMvcTest {
    @Autowired MockMvc mockMvc;
    @Autowired JwtService jwtService;
    @MockBean BankStatementService service;
    @MockBean AppUserRepository userRepository;

    private String token;

    @BeforeEach
    void setUp() {
        AppUser accounts = new AppUser();
        accounts.setId(43L);
        accounts.setPhone("accounts");
        accounts.setName("Accounts");
        accounts.setRole(Role.ACCOUNTS);
        token = jwtService.issueToken(accounts);
        when(userRepository.findById(43L)).thenReturn(Optional.of(accounts));
    }

    @Test
    void resolveTwiceReturns200Then409() throws Exception {
        BankStatementService.RowView resolved = row(BankRowStatus.RESOLVED);
        when(service.resolve(anyLong(), any(), any())).thenReturn(resolved)
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Bank row already resolved"));

        mockMvc.perform(resolve(241)).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVED"));
        mockMvc.perform(resolve(241)).andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("CONFLICT"))
                .andExpect(jsonPath("$.message").value("Bank row already resolved"));
    }

    @Test
    void rejectAfterResolveReturns409() throws Exception {
        when(service.reject(anyLong(), anyString(), any()))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Bank row already resolved"));
        mockMvc.perform(post("/api/accounts/queue/241/reject")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"duplicate\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("CONFLICT"));
    }

    @Test
    void resolveMatchedRowReturns409() throws Exception {
        when(service.resolve(anyLong(), any(), any()))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Bank row already resolved"));
        mockMvc.perform(resolve(236)).andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Bank row already resolved"));
    }

    @Test
    void unknownRowReturns404() throws Exception {
        when(service.resolve(anyLong(), any(), any()))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Bank row not found"));
        mockMvc.perform(resolve(99999)).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    void billOfAnotherFlatReturnsBadRequest() throws Exception {
        when(service.resolve(anyLong(), any(), any()))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bill does not belong to flat"));
        mockMvc.perform(resolve(241)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Bill does not belong to flat"));
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder resolve(long rowId) {
        return post("/api/accounts/queue/" + rowId + "/resolve")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"flatId\":3,\"billId\":9}");
    }

    private BankStatementService.RowView row(BankRowStatus status) {
        return new BankStatementService.RowView(241L, LocalDate.of(2026, 11, 3),
                500000L, "5000.00", "PART-T21-1601", status, 40, "test");
    }
}
