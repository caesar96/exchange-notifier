# Exchange Notifier — Design Document

> **Purpose of this document:** Platform-agnostic reference for the Exchange Notifier app. Written so the same product can be built in any stack (React Native, Flutter, web, etc.) without reading Android source code.

---

## 1. What the app does

Exchange Notifier tracks the **USD → MXN** exchange rate in real time and alerts the user when it crosses a configured threshold. It also draws a historical chart so the user can see how the rate has moved.

Core features:
- Display the current USD/MXN rate fetched from an external API
- Show a line chart for a selectable time period (Today, 1W, 1M, 1Y, Max)
- Alert the user via a push notification when the rate crosses a user-defined upper or lower threshold
- Poll the rate in the background at a configurable interval (minimum 15 min on Android due to OS constraints)
- Persist a local history of rate snapshots for the "Today" chart view
- Let the user choose a preferred data provider with automatic fallback

---

## 2. Screens

### 2.1 Main screen

**Layout (top to bottom):**
1. App bar — title "USD / MXN", settings gear icon top-right
2. Current rate — large bold number (e.g. `17.6197`)
3. Change indicator — colored triangle + value + percent change since the start of the selected period (green if positive, red if negative; hidden when only one data point exists)
4. Last-updated timestamp — e.g. `Updated: 20:36:09`
5. Period selector — five chips: Today | 1W | 1M | 1Y | Max
6. Line chart — area chart with gradient fill below the line, a dot at the last point, y-axis labels on the left, x-axis labels at first / middle / last point
7. Refresh button — manually triggers a rate fetch

**States:**
- **Loading** — spinner while the first fetch is in progress
- **Success** — shows all elements above
- **Error** — shows error message + last known rate (if any) + retry button

**Chart periods and their data source:**

| Period | Data source | Range |
|--------|-------------|-------|
| Today  | Local DB snapshots accumulated during the day | 00:00 → now |
| 1W     | Remote API time series | today − 7 days → today |
| 1M     | Remote API time series | today − 30 days → today |
| 1Y     | Remote API time series | today − 365 days → today |
| Max    | Remote API time series | today − 5 years → today |

The "Today" chart is a **live** view — it updates every time a new snapshot is written to the local DB by the background worker.

### 2.2 Settings screen

Sections (in order):

**ALERTS**
- Upper threshold — numeric text field + toggle; notification fires when rate rises above this value
- Lower threshold — numeric text field + toggle; notification fires when rate drops below this value
- Toggles are only enabled when a valid number is entered

**DATA SOURCE**
- Chip group (wrapping): Auto | Frankfurter | ExchangeRate-API | Currency API | Yahoo Finance
- "Auto" = try all providers in declaration order; first success wins
- Chips labeled "(latest only)" indicate the provider cannot supply historical series data

**MONITORING**
- Check interval — chip group: 15m | 30m | 1h | 2h
- Check now — immediately triggers a background rate fetch
- Test notification — fires a sample notification without any threshold check (for debugging)

**HISTORY**
- Retain data slider — 1–30 days; snapshots older than this are pruned by the background worker
- Clear history — deletes all local snapshots (confirmation dialog)

---

## 3. Data model

### 3.1 RateSnapshot

A single rate reading persisted locally.

| Field | Type | Description |
|-------|------|-------------|
| `rate` | Double | USD/MXN exchange rate |
| `timestamp` | Instant (UTC) | When the reading was taken |

### 3.2 RatePoint

A point in a time series returned by a remote API.

| Field | Type | Description |
|-------|------|-------------|
| `date` | LocalDate | Calendar date of the reading |
| `rate` | Double | USD/MXN exchange rate on that date |

### 3.3 AppPreferences (persisted across restarts)

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `upperThreshold` | Double? | null | Upper alert threshold |
| `lowerThreshold` | Double? | null | Lower alert threshold |
| `upperAlertEnabled` | Boolean | false | Whether upper alert is active |
| `lowerAlertEnabled` | Boolean | false | Whether lower alert is active |
| `pollIntervalMinutes` | Int | 15 | Background poll frequency |
| `lastKnownRate` | Double? | null | Persisted to detect threshold crossings across worker runs |
| `wasAboveUpper` | Boolean | false | Anti-spam: was the rate above the upper threshold on the last run? |
| `wasBelowLower` | Boolean | false | Anti-spam: was the rate below the lower threshold on the last run? |
| `historyRetentionDays` | Int | 7 | How many days of snapshots to keep in the local DB |
| `preferredProvider` | String | "auto" | ID of the preferred data provider |

---

## 4. Data providers

All providers expose the same two operations:
- `fetchLatestRate()` → `Double` (current USD/MXN rate)
- `fetchSeries(from, to)` → `List<RatePoint>` (daily historical series)

The **CompositeRateProvider** wraps all concrete providers. For any operation it:
1. Puts the user's preferred provider first in the attempt order
2. Tries each provider in turn; returns the first successful result
3. If all fail, returns the last error

For series requests, providers that only support latest are skipped automatically.

### Provider registry

| ID | Display name | Base URL | Supports series | Notes |
|----|--------------|----------|-----------------|-------|
| `frankfurter` | Frankfurter | `https://api.frankfurter.dev/v1/` | Yes | Free, no key |
| `exchangerate_api` | ExchangeRate-API | `https://open.er-api.com/v6/` | No | Free, no key |
| `currency_api` | Currency API | `https://currency-api.pages.dev/` | No | fawazahmed0, CDN mirror, free, no key |
| `yahoo_finance` | Yahoo Finance | `https://query2.finance.yahoo.com/` | No | Unofficial endpoint, requires User-Agent header |

### API contracts

#### Frankfurter — latest rate
```
GET /v1/latest?base=USD&symbols=MXN
→ { "rates": { "MXN": 17.6197 } }
```

#### Frankfurter — time series
```
GET /v1/{from}..{to}?base=USD&symbols=MXN
  where from/to = "YYYY-MM-DD"
→ { "rates": { "2024-06-17": { "MXN": 17.62 }, ... } }
```

#### ExchangeRate-API — latest rate
```
GET /v6/latest/USD
→ { "result": "success", "base_code": "USD", "rates": { "MXN": 17.62, ... } }
```

#### Currency API — latest rate
```
GET /v1/currencies/usd.json
→ { "date": "2024-06-24", "usd": { "mxn": 17.62, ... } }
```
Note: base currency code is **lowercase** in both path and response key.

#### Yahoo Finance — latest rate
```
GET /v7/finance/quote?symbols=USDMXN=X
→ { "quoteResponse": { "result": [{ "regularMarketPrice": 17.62 }] } }
```
Requires `User-Agent: Mozilla/5.0 (Linux; Android 10)` header or the server returns an error.

---

## 5. Background polling

The background worker runs on a periodic schedule (minimum 15 min, configurable to 30 min, 1 h, 2 h).

**Worker logic (in order):**
1. Fetch the latest rate via `CompositeRateProvider`
2. On network failure → retry (do not write a bad snapshot)
3. Insert a new `RateSnapshot` into the local DB
4. Delete snapshots older than `historyRetentionDays` days
5. Read `AppPreferences` to get current thresholds and last-run state
6. Evaluate crossings:
   - `nowAboveUpper = rate > upperThreshold`
   - `nowBelowLower = rate < lowerThreshold`
7. Fire "above upper" notification **only if** `nowAboveUpper && !wasAboveUpper` (edge trigger, not level trigger — prevents spamming while the rate stays crossed)
8. Fire "below lower" notification **only if** `nowBelowLower && !wasBelowLower`
9. Persist the new `lastKnownRate`, `wasAboveUpper`, `wasBelowLower` back to preferences
10. On success → done; on failure → retry

The alert scheduling rule: if **either** alert is enabled, schedule the worker; if **both** are disabled, cancel it. Re-scheduling is triggered whenever the user changes an alert toggle or the poll interval.

---

## 6. Notifications

Two alert types, each with a distinct directional icon:

| Event | Icon | Title | Body |
|-------|------|-------|------|
| Rate crossed above upper threshold | Rising chart + up-arrow | "USD/MXN rose above threshold" | "Rate: 17.6197 (upper threshold: 17.0000)" |
| Rate crossed below lower threshold | Falling chart + down-arrow | "USD/MXN fell below threshold" | "Rate: 17.6197 (lower threshold: 17.0000)" |
| Test (developer tool) | Rising chart | "Test notification" | "Alerts are working correctly ✓" |

Rate and threshold values are formatted to **4 decimal places**.

Tapping the notification opens the main screen.

Notification IDs are fixed (`1001` above, `1002` below, `1003` test) so a second crossing replaces the previous notification rather than stacking.

---

## 7. Local storage

One table: `rate_snapshots`

| Column | Type | Description |
|--------|------|-------------|
| `id` | INTEGER PRIMARY KEY AUTOINCREMENT | Row ID |
| `rate` | REAL | USD/MXN rate |
| `timestamp_millis` | INTEGER | Unix epoch milliseconds |

Queries used:
- Insert a new snapshot
- Observe all snapshots after a given timestamp (live, updates the Today chart in real time)
- Delete snapshots older than a given timestamp (retention pruning)
- Delete all snapshots (clear history action)

---

## 8. Preferences storage

Key-value store (Android DataStore / AsyncStorage equivalent):

| Key | Type |
|-----|------|
| `upper_threshold` | Double |
| `lower_threshold` | Double |
| `upper_alert_enabled` | Boolean |
| `lower_alert_enabled` | Boolean |
| `poll_interval_minutes` | Int |
| `last_known_rate` | Double |
| `was_above_upper` | Boolean |
| `was_below_lower` | Boolean |
| `history_retention_days` | Int |
| `preferred_provider` | String |

---

## 9. App architecture

```
UI layer
  MainScreen ──────────── MainViewModel
  SettingsScreen ──────── SettingsViewModel
        │                        │
        └────────────────────────┘
                   │
           Domain layer
     ExchangeRateRepository (interface)
     RateProvider (interface)
                   │
           Data layer
     FrankfurterRepository  ←── CompositeRateProvider
                                  ├── FrankfurterProvider   → FrankfurterApi (Retrofit)
                                  ├── ExchangeRateApiProvider → ExchangeRateApi (Retrofit)
                                  ├── CurrencyApiProvider   → CurrencyApi (Retrofit)
                                  └── YahooFinanceProvider  → YahooFinanceApi (Retrofit)
     RateSnapshotDao  (SQLite / Room)
     PreferencesRepository  (DataStore)

Background
     RateCheckWorker (WorkManager periodic task)
       └── ExchangeRateRepository
       └── PreferencesRepository
       └── NotificationHelper
```

**Key design rules:**
- The repository owns local persistence (DB write, timestamp stamping); providers are pure network adapters
- ViewModels do not know which provider is active; they only call the repository
- Threshold crossing is stateful — the last-run state is persisted in preferences, not in memory
- Notifications fire on the **edge** of a crossing, not while the rate remains crossed

---

## 10. Localization

Two locales supported: English (default) and Spanish.

All user-visible strings are externalized (no hardcoded strings in code). Provider names (Frankfurter, ExchangeRate-API, Currency API, Yahoo Finance) are brand names and are **not** translated.

---

## 11. Theme

Two themes: light and dark (follows system setting).

Chart color palette:

| State | Light mode | Dark mode |
|-------|-----------|-----------|
| Positive change | `#1B5E20` (dark green) | `#66BB6A` (light green) |
| Negative change | `#B71C1C` (dark red) | `#EF5350` (light red) |

Chart area fill uses a vertical gradient from the line color (top, ~40% opacity) to transparent (bottom).

---

