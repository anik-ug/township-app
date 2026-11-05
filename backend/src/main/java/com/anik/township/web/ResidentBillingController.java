package com.anik.township.web;

import com.anik.township.auth.AuthUser;
import com.anik.township.billing.BillingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/resident")
public class ResidentBillingController {

    private final BillingService billingService;

    public ResidentBillingController(BillingService billingService) {
        this.billingService = billingService;
    }

    @GetMapping("/home")
    public BillingService.HomeResponse home(@AuthenticationPrincipal AuthUser authUser) {
        return billingService.home(flatId(authUser));
    }

    @GetMapping("/bills")
    public List<BillingService.BillView> bills(@AuthenticationPrincipal AuthUser authUser) {
        return billingService.bills(flatId(authUser));
    }

    @GetMapping("/ledger")
    public List<BillingService.LedgerEntry> ledger(@AuthenticationPrincipal AuthUser authUser) {
        return billingService.ledger(flatId(authUser));
    }

    @PostMapping("/bills/{id}/pay")
    public BillingService.Receipt pay(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable long id,
            @Valid @RequestBody PaymentRequest request) {
        return billingService.pay(flatId(authUser), id, request.amountPaise());
    }

    @GetMapping("/receipts/{paymentId}")
    public BillingService.Receipt receipt(
            @AuthenticationPrincipal AuthUser authUser, @PathVariable long paymentId) {
        return billingService.receipt(flatId(authUser), paymentId);
    }

    private long flatId(AuthUser authUser) {
        if (authUser == null || authUser.getUser().getFlat() == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN, "Resident has no flat");
        }
        return authUser.getUser().getFlat().getId();
    }

    public record PaymentRequest(
            @NotNull @Positive(message = "amountPaise must be greater than zero") Long amountPaise) {
    }
}
