package com.anik.township.auth;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.anik.township.domain.AppUser;
import com.anik.township.repository.AppUserRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class OtpServiceTest {

    @Test
    void locksPhoneAfterFiveWrongAttemptsAndRejectsSixth() {
        AppUserRepository repository = mock(AppUserRepository.class);
        when(repository.findByPhone("9000000001")).thenReturn(Optional.of(new AppUser()));
        OtpService service = new OtpService(
                repository,
                mock(JwtService.class),
                "123456",
                Clock.fixed(Instant.parse("2026-11-05T00:00:00Z"), ZoneOffset.UTC));

        for (int attempt = 0; attempt < 5; attempt++) {
            assertThrows(InvalidOtpException.class, () -> service.verify("9000000001", "000000"));
        }

        assertThrows(OtpLockedException.class, () -> service.verify("9000000001", "123456"));
    }
}
