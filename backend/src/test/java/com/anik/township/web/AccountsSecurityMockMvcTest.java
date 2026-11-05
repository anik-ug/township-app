package com.anik.township.web;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.anik.township.accounts.BankStatementService;
import com.anik.township.auth.JwtAuthenticationFilter;
import com.anik.township.auth.JwtService;
import com.anik.township.config.SecurityConfig;
import com.anik.township.domain.AppUser;
import com.anik.township.domain.Role;
import com.anik.township.repository.AppUserRepository;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AccountsBankController.class)
@Import({SecurityConfig.class, JwtService.class, JwtAuthenticationFilter.class})
@TestPropertySource(properties = {
        "app.jwt-secret=test-jwt-secret-that-is-at-least-32-chars",
        "app.cors-origins=http://localhost:5173"
})
class AccountsSecurityMockMvcTest {
    @Autowired MockMvc mockMvc;
    @Autowired JwtService jwtService;
    @MockBean BankStatementService service;
    @MockBean AppUserRepository userRepository;

    static Stream<Arguments> accountsRequests() {
        return Stream.of(
                Arguments.of(get("/api/accounts/uploads/1")),
                Arguments.of(get("/api/accounts/queue")),
                Arguments.of(post("/api/accounts/queue/1/resolve")
                        .contentType("application/json").content("{\"flatId\":3,\"billId\":9}")),
                Arguments.of(post("/api/accounts/queue/1/reject")
                        .contentType("application/json").content("{\"reason\":\"not mine\"}")));
    }

    @ParameterizedTest
    @MethodSource("accountsRequests")
    void noTokenIsUnauthorizedOnEveryAccountsEndpoint(
            org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request) throws Exception {
        mockMvc.perform(request).andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @MethodSource("accountsRequests")
    void residentAndHelpdeskAreForbiddenOnEveryAccountsEndpoint(
            org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request) throws Exception {
        AppUser resident = user(1L, Role.RESIDENT, "resident");
        AppUser helpdesk = user(41L, Role.HELPDESK, "helpdesk");
        when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(resident));
        when(userRepository.findById(41L)).thenReturn(java.util.Optional.of(helpdesk));
        mockMvc.perform(request.header("Authorization", "Bearer " + jwtService.issueToken(resident)))
                .andExpect(status().isForbidden());
        mockMvc.perform(request.header("Authorization", "Bearer " + jwtService.issueToken(helpdesk)))
                .andExpect(status().isForbidden());
    }

    @Test
    void uploadEndpointAlsoRequiresAccountsRole() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "x.csv", "text/csv",
                "date,amount,reference,upi_id,narration\n".getBytes());
        AppUser resident = user(1L, Role.RESIDENT, "resident-upload");
        when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(resident));
        mockMvc.perform(multipart("/api/accounts/bank-upload").file(file)
                        .header("Authorization", "Bearer " + jwtService.issueToken(resident)))
                .andExpect(status().isForbidden());
        mockMvc.perform(multipart("/api/accounts/bank-upload").file(file))
                .andExpect(status().isUnauthorized());
    }

    private static AppUser user(long id, Role role, String phone) {
        AppUser user = new AppUser();
        user.setId(id);
        user.setPhone(phone);
        user.setName(role.name());
        user.setRole(role);
        return user;
    }
}
