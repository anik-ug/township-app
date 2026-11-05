package com.anik.township.web;

import com.anik.township.auth.AuthUser;
import com.anik.township.auth.InvalidOtpException;
import com.anik.township.auth.OtpLockedException;
import com.anik.township.auth.OtpService;
import com.anik.township.domain.AppUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.util.Map;
import java.util.LinkedHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final OtpService otpService;

    public AuthController(OtpService otpService) {
        this.otpService = otpService;
    }

    @PostMapping("/request-otp")
    public Map<String, String> requestOtp(@Valid @RequestBody PhoneRequest request) {
        otpService.requestOtp(request.phone());
        return Map.of("message", "OTP sent");
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        try {
            AppUser user = otpService.verify(request.phone(), request.otp());
            return ResponseEntity.ok(AuthResponses.token(otpService.issueToken(user), user));
        } catch (OtpLockedException exception) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("error", "TOO_MANY_REQUESTS", "message", "Too many OTP attempts"));
        } catch (InvalidOtpException exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "UNAUTHORIZED", "message", "Invalid phone or OTP"));
        }
    }

    @GetMapping("/me")
    public Map<String, Object> me(@AuthenticationPrincipal AuthUser authUser) {
        return AuthResponses.user(authUser.getUser());
    }

    public record PhoneRequest(
            @NotBlank @Pattern(regexp = "[6-9][0-9]{9}", message = "phone must be a valid 10 digit number")
            String phone) {
    }

    public record VerifyOtpRequest(
            @NotBlank @Pattern(regexp = "[6-9][0-9]{9}", message = "phone must be a valid 10 digit number")
            String phone,
            @NotBlank String otp) {
    }

    static final class AuthResponses {
        private AuthResponses() {
        }

        static Map<String, Object> token(String token, AppUser user) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("token", token);
            response.put("role", user.getRole().name());
            response.put("name", user.getName());
            response.put("flatNo", user.getFlat() == null ? null : user.getFlat().getFlatNo());
            return response;
        }

        static Map<String, Object> user(AppUser user) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("id", user.getId());
            response.put("name", user.getName());
            response.put("role", user.getRole().name());
            response.put("flatNo", user.getFlat() == null ? null : user.getFlat().getFlatNo());
            return response;
        }
    }
}
