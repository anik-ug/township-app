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
@RequestMapping("/api/helpdesk/complaints")
public class HelpdeskComplaintController {
    private final ComplaintService service;
    public HelpdeskComplaintController(ComplaintService service) { this.service = service; }

    @GetMapping
    public List<ComplaintService.View> list(@RequestParam(required = false) String status,
            @RequestParam(required = false) String category, @RequestParam(required = false) String urgency,
            @RequestParam(required = false) Boolean escalated, @RequestParam(required = false) String tower,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "50") int size) {
        List<ComplaintService.View> all = service.helpdeskList(status, category, urgency, escalated, tower);
        int from = Math.min(Math.max(0, page) * Math.max(1, size), all.size());
        return all.subList(from, Math.min(from + Math.max(1, size), all.size()));
    }
    @PatchMapping("/{id}/category")
    public ComplaintService.View category(@AuthenticationPrincipal AuthUser user, @PathVariable long id,
            @Valid @RequestBody CategoryRequest request) {
        return service.changeCategory(id, request.category(), user.getUser());
    }
    @PatchMapping("/{id}/status")
    public ComplaintService.View status(@AuthenticationPrincipal AuthUser user, @PathVariable long id,
            @Valid @RequestBody StatusRequest request) {
        return service.changeStatus(id, ComplaintStatus.valueOf(request.status()), user.getUser());
    }
    @PatchMapping("/{id}/assign")
    public ComplaintService.View assign(@AuthenticationPrincipal AuthUser user, @PathVariable long id,
            @Valid @RequestBody AssignRequest request) {
        return service.assign(id, request.assigneeId(), user.getUser());
    }
    public record CategoryRequest(@NotBlank @Pattern(regexp = "PLUMBING|ELECTRICAL|LIFT|SECURITY|HOUSEKEEPING|PARKING|OTHER") String category) {}
    public record StatusRequest(@NotBlank @Pattern(regexp = "OPEN|ASSIGNED|IN_PROGRESS|RESOLVED|CLOSED") String status) {}
    public record AssignRequest(@NotNull @Positive Long assigneeId) {}
}
