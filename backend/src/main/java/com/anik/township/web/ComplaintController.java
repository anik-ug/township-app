package com.anik.township.web;

import com.anik.township.auth.AuthUser;
import com.anik.township.complaints.ComplaintService;
import com.anik.township.domain.ComplaintStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/resident/complaints")
public class ComplaintController {
    private final ComplaintService service;
    public ComplaintController(ComplaintService service) { this.service = service; }

    @PostMapping
    public ComplaintService.View create(@AuthenticationPrincipal AuthUser user, @Valid @RequestBody CreateRequest request) {
        return service.create(user.getUser(), request.description(), request.category());
    }
    @GetMapping
    public List<ComplaintService.View> list(@AuthenticationPrincipal AuthUser user) {
        return service.residentList(user.getUser().getFlat().getId());
    }
    @GetMapping("/{id}")
    public ComplaintService.View get(@AuthenticationPrincipal AuthUser user, @PathVariable long id) {
        return service.residentGet(user.getUser().getFlat().getId(), id);
    }
    @PostMapping("/{id}/reopen")
    public ComplaintService.View reopen(@AuthenticationPrincipal AuthUser user, @PathVariable long id) {
        return service.reopen(user.getUser().getFlat().getId(), id, user.getUser());
    }
    @PostMapping("/{id}/rate")
    public ComplaintService.View rate(@AuthenticationPrincipal AuthUser user, @PathVariable long id,
            @Valid @RequestBody RateRequest request) {
        return service.rate(user.getUser().getFlat().getId(), id, request.rating());
    }
    public record CreateRequest(@NotBlank @Size(min = 10, max = 1000) String description,
            @Pattern(regexp = "PLUMBING|ELECTRICAL|LIFT|SECURITY|HOUSEKEEPING|PARKING|OTHER") String category) {}
    public record RateRequest(@NotNull @Min(1) @Max(5) Short rating) {}
}
