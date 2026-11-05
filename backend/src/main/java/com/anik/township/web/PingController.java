package com.anik.township.web;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PingController {

    @GetMapping("/api/resident/ping")
    public Map<String, Object> resident() {
        return Map.of("ok", true, "role", "RESIDENT");
    }

    @GetMapping("/api/helpdesk/ping")
    public Map<String, Object> helpdesk() {
        return Map.of("ok", true, "role", "HELPDESK");
    }

    @GetMapping("/api/accounts/ping")
    public Map<String, Object> accounts() {
        return Map.of("ok", true, "role", "ACCOUNTS");
    }

    @GetMapping("/api/committee/ping")
    public Map<String, Object> committee() {
        return Map.of("ok", true, "role", "COMMITTEE");
    }
}
