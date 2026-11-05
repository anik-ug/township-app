# Project: Gated Township Resident App

Take-home project: resident app for a gated township in Pune (2,400 flats, 28 towers).

## Stack
- Backend: Spring Boot 3 (Java 17, Maven), PostgreSQL + Flyway
- Frontend: React + Vite + Tailwind (mobile-first)

## Rules
- Money is always BIGINT paise (long in Java). Never float or double. Rounding is HALF_UP.
- Bill = area_sqft x 6.50 per month. Late fee comes from the late_fee_rule table and is computed, not stored.
- Date logic uses a configurable "as-of date" (app.as-of-date, empty = today) so demo numbers stay fixed.
- Roles: RESIDENT, HELPDESK, ACCOUNTS, COMMITTEE (committee is read-only). Every endpoint has a role check, and a resident can only see their own flat's data.
- Validate all input (jakarta.validation). No secrets in the repo; everything via environment variables.
- Wherever AI can be wrong, use a safe fallback: unclear payments are never auto-posted, safety words (fire, gas, lift stuck, shock) trigger rule-based escalation that no AI output can override, duplicate complaints are only linked, never closed.
- Small, clean commits. Unit tests for all core logic.
- Do not store real personal data. Seed data must be fake.