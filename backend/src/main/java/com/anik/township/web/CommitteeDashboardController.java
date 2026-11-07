package com.anik.township.web;

import com.anik.township.committee.CommitteeDashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/committee")
public class CommitteeDashboardController {
    private final CommitteeDashboardService service;

    public CommitteeDashboardController(CommitteeDashboardService service) {
        this.service = service;
    }

    @GetMapping("/dashboard")
    public com.anik.township.committee.DashboardCalculator.Dashboard dashboard() {
        return service.dashboard();
    }
}
