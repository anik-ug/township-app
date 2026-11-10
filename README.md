# Township: Gated Township Resident App

A resident app for a gated township in Pune (2,400 flats, 28 towers), built as a take-home for the
*Software Engineering Intern* role (Problem Statement 4).

Residents pay maintenance, raise complaints and always see where a complaint stands. The back office
matches bank payments by itself and never posts an unclear payment to a flat. The committee gets a
monthly view it can trust.

> Everything here runs on **fake, seeded data**. OTP, payments and messages are mocked.

## What works end to end

| Area | What it does |
|---|---|
| Resident login | Mobile number + OTP (mock), linked to a flat. Roles: `RESIDENT`, `HELPDESK`, `ACCOUNTS`, `COMMITTEE`. |
| Bills and payment | Bill = area x Rs 6.50. Configurable late fee (read from the database). Test payment, receipt, per-flat ledger. |
| Bank statement matching | Upload a bank CSV. Exact matches are posted; everything else goes to an accounts queue with a confidence score and a plain-language reason. |
| Complaints + AI | Category and urgency, auto-assignment, status timeline, duplicate linking, help-desk one-tap category fix. |
| Safety rule | Words like *fire, gas leak, lift stuck, shock* always escalate to the duty manager, whatever the AI says. |
| Committee dashboard | Collections, dues ageing, complaint response and resolution times (read only). |
| Languages | English, Hindi, Marathi. |

## Run it in one command

You need Docker. From the repository root:

```bash
JWT_SECRET=$(openssl rand -hex 24) docker compose -f docker-compose.demo.yml up --build
```

Open <http://localhost:5173> and log in with `9000000002` and OTP `123456`.
If you have run a dev server on `localhost:5173` before, use a private/incognito window or clear site data
(an old service worker can otherwise show a blank page).

More options, demo logins, environment variables and a 5-minute tour are in **[deployment.md](deployment.md)**.
The design and decisions are in the Technical Design Document (TDD) submitted with this project.

## Tech stack

- **Backend:** Java 17, Spring Boot 3.3, Spring Security (stateless JWT), Spring Data JPA, Flyway
- **Database:** PostgreSQL 16
- **Frontend:** React, Vite, TypeScript, Tailwind CSS, react-i18next (installable web app)
- **Tests:** JUnit 5 + MockMvc (backend), Vitest (frontend)
- **Packaging:** Docker, Docker Compose

## Project structure

```
backend/                 Spring Boot service
  src/main/java/com/anik/township/
    auth/                OTP login, JWT, rate limiting
    billing/             bills, late fee calculator, payments, ledger
    accounts/            bank statement matcher, upload service, accounts queue
    complaints/          safety rules, classifiers, assignment, workflow
    domain/, repository/ entities and Spring Data repositories
    web/                 controllers per role, error handling
  src/main/resources/db/migration/   Flyway: schema, seed data, payment allocation, matching, complaints
  src/test/              unit, MockMvc and integration tests
frontend/                React app
  src/pages/resident/    login, home, bills, pay, receipt, complaints
  src/pages/staff/       help desk, accounts, committee
  src/components/, layouts/, lib/, hooks/, locales/ (en, hi, mr)
docs/sample_bank.csv     sample bank statement for the matching demo
docker-compose.yml       PostgreSQL for local development
docker-compose.demo.yml  database + backend + frontend, one command
```

## Key design decisions

- **Money is integer paise** (`BIGINT` / `long`). No floating point in billing or matching. Rounding is HALF_UP.
- **Late fee is computed, not stored**: simple interest on the unpaid principal after a grace period, from the
  `late_fee_rule` table, so the society bye-law can change without a deploy.
- **A fixed "as-of" date** (default `2026-11-05`) keeps the demo numbers equal to the brief (47 and 18 days overdue).
- **Matching is rule-based and explainable.** Only two rules can post a payment (flat number + exact amount, or a
  unique UPI id + exact amount). Partial, wrong, ambiguous and unknown payments are queued. A queued row is never
  posted automatically; the accountant resolves it once (a second attempt returns `409`) and every action is audited.
- **AI is never the last word.** The safety rule runs before the classifier and cannot be overridden. Duplicates are
  linked, never auto-closed. The help desk can change a category in one tap. The optional LLM classifier falls back to
  the keyword classifier on any error.
- **Access control:** the flat always comes from the token; a resident asking for another flat's data gets `404`.

## Tests

```bash
cd backend && ./mvnw test      # needs PostgreSQL running (docker compose up -d)
cd frontend && npm test
```

## Known limits

- OTP, payment gateway and messages are mocked.
- Visitor management and amenity booking are not built (optional depth in the brief).
- Hindi and Marathi texts are machine-generated and not reviewed by native speakers.
- A free-tier backend host sleeps and wakes slowly; the one-command Docker stack is the supported way to run the app.

See the TDD for the full list of assumptions, what was cut and what I would do next.