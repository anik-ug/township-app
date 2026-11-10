# deployment.md

Repository: <https://github.com/anik-ug/township-app>

This file explains how to run the app locally, how it is deployed, which environment variables it needs
(names only, never values), and the demo logins for the reviewer.

## 1. Fastest way: one command (recommended)

Requirements: Docker Desktop (or Docker Engine with the Compose plugin).

From the repository root:

```bash
JWT_SECRET=$(openssl rand -hex 24) docker compose -f docker-compose.demo.yml up --build
```

- The first build takes 5 to 10 minutes (Maven and npm downloads). Later starts take 1 to 2 minutes.
- Wait for `Started TownshipApplication` in the logs, then open <http://localhost:5173>.
- The API is on <http://localhost:8090>.
- Flyway creates the schema and the seed data automatically on first start.
- Stop and reset everything: `docker compose -f docker-compose.demo.yml down -v`

`JWT_SECRET` is required by the compose file so that no secret is stored in the repository. Any random value of
32 or more characters works.

**Blank page?** If you ever ran a development server on `localhost:5173`, your browser may hold an old service
worker. Open the app in a private/incognito window, or clear site data for `localhost:5173`.

## 2. Run for development (without the demo compose file)

Requirements: Java 17, Node 20, Docker (for PostgreSQL).

```bash
# 1) database (PostgreSQL 16 on port 5432)
docker compose up -d

# 2) backend (port 8090 here, because 8080 is often taken)
cd backend
SERVER_PORT=8090 ./mvnw spring-boot:run

# 3) frontend, in another terminal
cd frontend
npm install
npm run dev            # http://localhost:5173
```

The defaults in `backend/src/main/resources/application.yml` point to the local database and a local-only JWT
secret, so no variables are needed for development. The frontend calls `http://localhost:8090` unless
`VITE_API_URL` is set.

Run the tests:

```bash
cd backend && ./mvnw test      # PostgreSQL must be running
cd frontend && npm test
```

## 3. How it is deployed

- **Local / review:** `docker-compose.demo.yml` runs three containers: PostgreSQL 16, the Spring Boot backend
  (`backend/Dockerfile`) and the React app served by nginx (`frontend/Dockerfile`). This is the supported way
  to review the project.
- **Hosted backend (optional, best effort):** the backend is also deployed as a Docker web service on Render's
  free tier with a Neon PostgreSQL database: <https://township-app-3rrg.onrender.com>.
  The free tier sleeps when idle, so the first request can take 2 to 3 minutes. Opening the root URL returns
  `401 Authentication required`, which is the expected answer for an unauthenticated request.
  The web UI is **not** hosted publicly; run it with the command in section 1.

## 4. Environment variables (names only)

Backend:

| Name | Purpose | Default (development) |
|---|---|---|
| `DB_URL` | JDBC URL of PostgreSQL | local database |
| `DB_USER` | Database user | local user |
| `DB_PASSWORD` | Database password | local password |
| `JWT_SECRET` | Signing key for tokens (32+ characters). **Set it in any shared deployment.** | local-only default |
| `DEMO_OTP` | The mock OTP accepted for every user | `123456` |
| `AS_OF_DATE` | Date used for all "today" logic, so demo numbers stay fixed | `2026-11-05` |
| `CORS_ORIGINS` | Allowed browser origin(s) | `http://localhost:5173` |
| `DUTY_MANAGER_PHONE` | Help-desk user who receives safety escalations | `9100000001` |
| `SLA_RESPONSE_MINUTES` | Complaint response SLA | `240` |
| `CLASSIFIER_API_KEY` | Optional. Enables the LLM classifier; without it the keyword classifier is used | empty |
| `CLASSIFIER_BASE_URL`, `CLASSIFIER_MODEL` | Optional LLM endpoint and model | see application.yml |
| `LOG_LEVEL_WEB`, `LOG_LEVEL_SECURITY` | Log levels | `INFO` |
| `PORT` / `SERVER_PORT` | HTTP port | `8080` |
| `JAVA_OPTS` | JVM options (used by the Docker image) | `-Xmx320m -XX:+UseSerialGC` |

Frontend (build time):

| Name | Purpose |
|---|---|
| `VITE_API_URL` | Base URL of the backend API |
| `VITE_SHOW_DEMO_HINT` | `true` shows the demo OTP hint on the login screen |

No secret values are stored in the repository. `.env` files are git-ignored; `.env.example` lists names only.

## 5. Demo logins

The OTP for **every** user is `123456`. All people and numbers are fake.

| Role | Phone | Name / flat | What to look at |
|---|---|---|---|
| Resident | `9000000002` | Swati Joshi, T12-0408 (tenant) | Rs 6,447.49 due, 47 days overdue, a water-leak complaint |
| Resident | `9000000003` | Aamir Shaikh, T21-1601 | Part paid, Rs 5,674.89 due, 18 days overdue |
| Resident | `9000000001` | Neeraj Kulkarni, T07-1203 | Fully paid |
| Help desk | `9100000001` | Mohan Bapat (duty manager) | Complaint queue, escalated rows first |
| Help desk | `9100000002` | Help-desk staff | Same pages |
| Accounts | `9100000003` | Accounts staff | Bank upload and accounts queue |
| Committee | `9100000004` | Committee member (read only) | Dashboard |

## 6. Five-minute tour for the reviewer

1. **Resident (9000000002):** Home shows Rs 6,447.49 and "47 days overdue". Open Pay and type letters or an amount
   above the due: input is blocked and the button stays disabled. (Paying changes the seed data; restart with
   `down -v` to reset.)
2. **Safety rule:** as the same resident, raise a complaint "gas smell in the kitchen". It is marked Critical, the
   timeline shows the escalation to the duty manager. Try "the fireplace decoration is dusty": no escalation.
3. **Help desk (9100000001):** escalated complaints are first. Change a category in one tap; the escalation stays.
   Try an invalid status jump (Assigned to Closed): rejected.
4. **Accounts (9100000003):** upload `docs/sample_bank.csv`. Expect 5 matched, 6 queued, 1 duplicate, 1 error,
   each row with a confidence and a "why". The Rs 5,000 payment from T21-1601 is queued, not posted. Resolve one
   queued row, then try again: a second resolve is rejected. Upload the same file again: every row is a duplicate.
5. **Committee (9100000004):** collections, dues ageing (T12-0408 in 31-60 days, T21-1601 in 0-30) and complaint
   times. Switch the language to हिन्दी or मराठी.

## 7. Resetting the data

The demo data is changed by payments, complaints and uploads. To get the original seed back:

```bash
docker compose -f docker-compose.demo.yml down -v
JWT_SECRET=$(openssl rand -hex 24) docker compose -f docker-compose.demo.yml up --build
```
