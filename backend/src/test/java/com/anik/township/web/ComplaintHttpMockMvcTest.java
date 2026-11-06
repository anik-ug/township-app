package com.anik.township.web;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.anik.township.auth.*;
import com.anik.township.complaints.ComplaintService;
import com.anik.township.config.SecurityConfig;
import com.anik.township.domain.*;
import com.anik.township.repository.AppUserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.server.ResponseStatusException;

@WebMvcTest(controllers = {ComplaintController.class, HelpdeskComplaintController.class})
@Import({SecurityConfig.class, JwtService.class, JwtAuthenticationFilter.class, GlobalExceptionHandler.class})
@TestPropertySource(properties = {
        "app.jwt-secret=test-jwt-secret-that-is-at-least-32-chars",
        "app.cors-origins=http://localhost:5173"
})
class ComplaintHttpMockMvcTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ComplaintService service;

    @MockBean
    private AppUserRepository userRepository;

    private AppUser resident;
    private AppUser otherResident;
    private AppUser helpdesk;

    @BeforeEach
    void setUp() {
        resident = testUser(1L, Role.RESIDENT, 10L);
        otherResident = testUser(2L, Role.RESIDENT, 20L);
        helpdesk = testUser(3L, Role.HELPDESK, 0L);
    }

    @Test
    void anotherFlatsComplaintReturns404() throws Exception {
        when(service.residentGet(10L, 99L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Complaint not found"));

        mockMvc.perform(get("/api/resident/complaints/99").with(auth(resident)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Complaint not found"));
    }

    @Test
    void residentCannotAccessHelpdesk() throws Exception {
        mockMvc.perform(get("/api/helpdesk/complaints").with(auth(resident)))
                .andExpect(status().isForbidden());
    }

    @Test
    void helpdeskCannotAccessResident() throws Exception {
        mockMvc.perform(get("/api/resident/complaints").with(auth(helpdesk)))
                .andExpect(status().isForbidden());
    }

    @Test
    void noTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/resident/complaints"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Authentication required"));
    }

    @Test
    void invalidTransitionReturnsStandardConflictBody() throws Exception {
        when(service.changeStatus(eq(99L), eq(ComplaintStatus.CLOSED), any()))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Invalid complaint status transition"));

        mockMvc.perform(patch("/api/helpdesk/complaints/99/status")
                        .with(auth(helpdesk))
                        .contentType("application/json")
                        .content("{\"status\":\"CLOSED\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("CONFLICT"))
                .andExpect(jsonPath("$.message").value("Invalid complaint status transition"));
    }

    @Test
    void shortDescriptionReturns400() throws Exception {
        mockMvc.perform(post("/api/resident/complaints")
                        .with(auth(resident))
                        .contentType("application/json")
                        .content("{\"description\":\"short\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Request validation failed"));
        verifyNoInteractions(service);
    }

    private static AppUser testUser(long id, Role role, long flatId) {
        AppUser user = new AppUser();
        user.setId(id);
        user.setPhone("90000000" + id);
        user.setName(role.name());
        user.setRole(role);
        if (role == Role.RESIDENT) {
            Flat flat = new Flat();
            flat.setId(flatId);
            flat.setTower("T01");
            user.setFlat(flat);
        }
        return user;
    }

    private static RequestPostProcessor auth(AppUser appUser) {
        return authentication(new UsernamePasswordAuthenticationToken(
                new AuthUser(appUser), null,
                java.util.List.of(new SimpleGrantedAuthority("ROLE_" + appUser.getRole().name()))));
    }
}
