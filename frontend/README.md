# Township App

## Run

```bash
npm install
npm run dev
```

Set `VITE_API_URL` to the backend base URL (defaults to `http://localhost:8090`). Set `VITE_SHOW_DEMO_HINT=true` to show the demo OTP hint on the OTP step.

Staff use the same OTP login. Helpdesk, Accounts, and Committee users are redirected to
their role dashboards; resident routes remain restricted to residents. The staff screens
use `/api/helpdesk/complaints`, `/api/accounts/bank-upload`, `/api/accounts/queue`, and
`/api/committee/dashboard`. Money is displayed from paise values only, and all dates use
Asia/Kolkata.

## Build and tests

```bash
npm run build
npm test
```
