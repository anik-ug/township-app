package com.anik.township.auth;

import com.anik.township.domain.AppUser;
import com.anik.township.repository.AppUserRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class OtpService {

    private static final int MAX_WRONG_ATTEMPTS = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(10);

    private final AppUserRepository userRepository;
    private final JwtService jwtService;
    private final String demoOtp;
    private final Clock clock;
    private final Map<String, AttemptState> attempts = new ConcurrentHashMap<>();

    @Autowired
    public OtpService(
            AppUserRepository userRepository,
            JwtService jwtService,
            @Value("${app.demo-otp}") String demoOtp) {
        this(userRepository, jwtService, demoOtp, Clock.systemUTC());
    }

    OtpService(AppUserRepository userRepository, JwtService jwtService, String demoOtp, Clock clock) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.demoOtp = demoOtp;
        this.clock = clock;
    }

    public boolean isLocked(String phone) {
        AttemptState state = attempts.get(phone);
        if (state == null) {
            return false;
        }
        if (state.lockedUntil != null && Instant.now(clock).isBefore(state.lockedUntil)) {
            return true;
        }
        if (state.lockedUntil != null) {
            attempts.remove(phone, state);
        }
        return false;
    }

    public void requestOtp(String phone) {
        userRepository.findByPhone(phone);
    }

    public AppUser verify(String phone, String otp) {
        if (isLocked(phone)) {
            throw new OtpLockedException();
        }

        AppUser user = userRepository.findByPhone(phone).orElse(null);
        if (user == null || !demoOtp.equals(otp)) {
            registerFailure(phone);
            throw new InvalidOtpException();
        }

        attempts.remove(phone);
        return user;
    }

    public String issueToken(AppUser user) {
        return jwtService.issueToken(user);
    }

    private void registerFailure(String phone) {
        Instant now = Instant.now(clock);
        attempts.compute(phone, (key, current) -> {
            AttemptState state = current == null ? new AttemptState() : current;
            state.failures++;
            if (state.failures >= MAX_WRONG_ATTEMPTS) {
                state.lockedUntil = now.plus(LOCK_DURATION);
            }
            return state;
        });
    }

    private static class AttemptState {
        private int failures;
        private Instant lockedUntil;
    }
}
