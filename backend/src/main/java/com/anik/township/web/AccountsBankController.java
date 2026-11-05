package com.anik.township.web;

import com.anik.township.accounts.BankStatementService;
import com.anik.township.auth.AuthUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/accounts")
public class AccountsBankController {
    private final BankStatementService service;

    public AccountsBankController(BankStatementService service) {
        this.service = service;
    }

    @PostMapping("/bank-upload")
    public BankStatementService.UploadView upload(
            @RequestPart("file") MultipartFile file, @AuthenticationPrincipal AuthUser actor) {
        return service.upload(file, actor);
    }

    @GetMapping("/uploads/{id}")
    public BankStatementService.UploadView upload(@PathVariable long id) {
        return service.getUpload(id);
    }

    @GetMapping("/queue")
    public List<BankStatementService.QueueView> queue() {
        return service.queue();
    }

    @PostMapping("/queue/{rowId}/resolve")
    public BankStatementService.RowView resolve(@PathVariable long rowId,
            @Valid @RequestBody BankStatementService.ResolveRequest request,
            @AuthenticationPrincipal AuthUser actor) {
        return service.resolve(rowId, request, actor);
    }

    @PostMapping("/queue/{rowId}/reject")
    public BankStatementService.RowView reject(@PathVariable long rowId,
            @Valid @RequestBody RejectRequest request, @AuthenticationPrincipal AuthUser actor) {
        return service.reject(rowId, request.reason(), actor);
    }

    public record RejectRequest(@NotBlank String reason) {}
}
