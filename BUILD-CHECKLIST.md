# Patta — Pharmacy Store App · Master Build Checklist

> Single-store voice-first Android app for a retail medical/pharmacy shop.
> Offline-first. ₹0 budget. Data model kept multi-store ready for the future.
> This is the long-run source of truth — tick items as they get done, add notes inline.

**App name:** Patta
**Owner:** self-use (Sharma Medical Store as sample), multi-store later
**Design spec:** `design/stitch_stitch_pharmacy_app/` (19 screens) — implement in Compose

---

## 0. Tech Stack (LOCKED)

- [ ] **UI:** Kotlin + Jetpack Compose + Material 3
- [ ] **Local DB (source of truth):** Room (SQLite)
- [ ] **Async:** Kotlin Coroutines + Flow
- [ ] **DI:** Hilt
- [ ] **Architecture:** MVVM + Repository pattern, single-activity + Compose Navigation
- [ ] **STT:** Android SpeechRecognizer (hi-IN); offline fallback = Vosk small Hindi model
- [ ] **TTS:** Android TextToSpeech (hi-IN)
- [ ] **Barcode:** ML Kit Barcode Scanning + CameraX
- [ ] **PDF/print:** Android PdfDocument (bills, PO, statements)
- [ ] **Share:** WhatsApp Intent deep-links
- [ ] **Cloud sync/backup (later phase):** Supabase (Postgres) free tier + Supabase-kt
- [ ] **Backup (v1, no server):** local JSON/SQLite export to Google Drive / Downloads
- [ ] **Money:** store all amounts as paise (Long/BigDecimal), never Float

---

## 1. Foundation & Project Setup

- [ ] Create Android Studio project (min SDK 26 / target latest), package `com.patta.pharmacy`
- [ ] Set up Gradle version catalog, add deps (Compose BOM, Room, Hilt, Coroutines, CameraX, ML Kit, Navigation)
- [ ] Git repo + `.gitignore` + branch strategy
- [ ] Theme: port design tokens from `design/.../DESIGN.md`
  - [ ] Colors: primary teal #00685b, money-in green, expiry amber, money-out red, surfaces
  - [ ] Typography: Plus Jakarta Sans (numbers/headings), Inter (body)
  - [ ] Shapes: 16px cards, 12px buttons, pill chips
  - [ ] Indian currency formatter (₹1,24,500) + paise↔rupee utils
- [ ] Reusable components: PrimaryButton, MoneyText, QtyStepper, SearchBarWithMic, StatTile, BottomNav (Home/Billing/Stock/Khata/More), BatchChip, AlertCard
- [ ] Bottom navigation scaffold with 5 tabs + routing; "More" opens menu hub
- [ ] App icon + splash (shows "Patta" — the only place app name appears; screen headers show SHOP name)
- [ ] Local PIN lock for the app (no full auth needed for single store)

---

## 2. Data Model (Room entities) — build sync-ready from day 1

Every entity carries: `id` (UUID), `storeId` (default single store, for future multi-store),
`createdAt`, `updatedAt`, `isDeleted` (soft delete), `syncedAt` (nullable, for later cloud sync).

- [ ] **Store** — id, name, drugLicenseNo, gstin, address, phone, logoUri
- [ ] **Medicine (master)** — id, name, salt/composition, company, packType (strip/bottle/box/tube),
      unitsPerPack, allowLooseSale, hsnCode, gstPercent, defaultMrp, rackLocation, reorderLevel,
      isScheduleH1, isFridgeItem, barcode (nullable)
- [ ] **Batch** — id, medicineId(FK), batchNo, expiryDate, qtyInStock (in base units),
      mrp, purchaseRate, landedCost, supplierId(FK), receivedDate
- [ ] **Bill (sale)** — id, billNo, dateTime, customerId(nullable FK), subtotal, gstAmount,
      discount, total, paymentMode (cash/upi/udhaar/split), amountPaid, staffId
- [ ] **BillItem** — id, billId(FK), medicineId, batchId, qty, unit (strip/tablet), rate, gstPercent, lineTotal
- [ ] **Customer** — id, name, phone, address, openingBalance, notes
- [ ] **CustomerLedgerEntry** — id, customerId(FK), date, type (bill/payment/adjustment),
      refBillId, amount (+/-), paymentMode, runningBalance, note
- [ ] **Supplier** — id, name, phone, address, gstin, creditPeriodDays, openingBalance
- [ ] **Purchase (invoice)** — id, supplierId(FK), invoiceNo, invoiceDate, dueDate,
      subtotal, gstAmount, discount, total, status (received/partial)
- [ ] **PurchaseItem** — id, purchaseId(FK), medicineId, batchNo, expiryDate, qty, freeQty,
      rate, schemeDiscount, gstPercent, landedCost, margin
- [ ] **SupplierLedgerEntry** — id, supplierId(FK), date, type (purchase/payment/creditNote),
      refId, amount (+/-), runningBalance, note
- [ ] **CreditNote (expiry/breakage return)** — id, supplierId, date, items, totalValue, status
- [ ] **MissedSale** — id, medicineName (free text or medicineId), timesAsked, lastAskedAt, resolved
- [ ] **PurchaseOrder** — id, supplierId, dateTime, status (draft/sent), items[], estimatedValue
- [ ] **PurchaseOrderItem** — id, poId(FK), medicineId, orderQty, reason (low/missed/fast)
- [ ] **DayClose** — id, date, expectedCash, actualCash, shortfall, totalSale, profit, billsCount
- [ ] **ScheduleH1Register** — id, billId, medicineId, patientName, doctorName, qty, date, rxPhotoUri
- [ ] **StockMovement (audit)** — id, medicineId, batchId, changeQty, reason (sale/purchase/return/adjust), refId, dateTime
- [ ] DAOs + Repositories for each; Flows for reactive UI
- [ ] Migration strategy set up (Room auto-migrations where possible)
- [ ] Seed/sample data for dev (keep batch numbers & dates CONSISTENT across screens)

---

## 3. PHASE 1 — Inventory + Billing (offline core) · ~Week 1–2

**Goal: shopkeeper can add medicines, hold stock in batches, and make a bill — fully offline.**

### 3a. Item master
- [ ] Add / Edit Medicine screen (form from `add_edit_medicine` design)
  - [ ] Identity: name, salt (auto-suggest), company
  - [ ] Pack: type chips, units per pack, loose-sale toggle
  - [ ] Pricing: MRP, purchase rate, GST% chips, HSN
  - [ ] Shelf: rack location, reorder level
  - [ ] Flags: Schedule H1, Fridge item
  - [ ] Voice fill ("Bolke bhar do") — optional, wire in Phase 4
- [ ] Stock list screen (`stock_list`) — grouped A–Z, filter chips (Sab/Kam stock/Near expiry/Dead stock/Fridge)
  - [ ] Stock-health bar (green/amber/red), near-expiry tag
  - [ ] Search (text now, mic in Phase 4), "+" FAB to add item
- [ ] Item detail screen (`item_detail`) — batches, landed cost, margin %, 30d movement, reorder stepper

### 3b. Batch & stock logic
- [ ] Batch-wise stock with expiry per batch
- [ ] Loose sale: strip↔tablet conversion (e.g. 0.4 strip)
- [ ] **FEFO** — earliest-expiry batch auto-selected in billing/pickers
- [ ] Every stock change writes a StockMovement audit row

### 3c. Billing (POS)
- [ ] New Sale screen (`new_sale_billing`) — search → add item → qty stepper → strip/tab toggle
- [ ] Item/batch picker (`item_picker`) — results with salt, in-stock, batch chips (FEFO), "Ye pehle becho"
- [ ] Salt-wise substitute suggest when out of stock (with margin %)
- [ ] Line-level: batch, near-expiry dot, edit qty, swipe-delete
- [ ] Bottom summary: subtotal, GST breakup, discount, big TOTAL
- [ ] Payment: Cash / UPI / Udhaar / Split
- [ ] On save: decrement batch stock (FEFO), write Bill + BillItems + StockMovement; if Udhaar → CustomerLedgerEntry
- [ ] Bill number generator (per-day sequence)
- [ ] Bill preview → PDF → WhatsApp/print/share
- [ ] Sale return: pick past bill → return items → stock back + ledger reverse

**Phase 1 done when:** add stock, make a cash + an udhaar bill, stock decrements correctly by batch, all offline.

---

## 4. PHASE 2 — Purchase + Suppliers · ~Week 3

- [ ] Suppliers list (`suppliers_payments`) — total dena hai, payment calendar strip, per-supplier outstanding, overdue tag
- [ ] Add / edit supplier (name, phone, GSTIN, credit period)
- [ ] Purchase entry (`purchase_entry`) — 3 steps: Supplier → Items → Confirm
  - [ ] Per item: batch, expiry (MM/YY), qty, free qty, rate, scheme discount, GST
  - [ ] **Landed cost auto-calc** (scheme + free goods + discount + GST) → real margin %
  - [ ] On save: create Batch rows, increment stock, SupplierLedgerEntry (+), StockMovement
  - [ ] Invoice-total mismatch warning ("₹120 ka fark")
- [ ] Supplier ledger (`supplier_ledger`) — purchases (+), payments (−), credit notes (−), running balance
  - [ ] Filter chips: Sab/Bills/Payments/Returns
  - [ ] Record payment (cash/UPI) → ledger entry
  - [ ] Statement share (WhatsApp/PDF)
- [ ] Payment-due dashboard: this week's dues, per-supplier due dates
- [ ] Purchase return / expiry return → Credit Note → adjust supplier ledger
- [ ] 3-way match aid (PO ↔ Invoice ↔ received) — flag rate/qty mismatch

---

## 5. PHASE 3 — Khata + Expiry + Reports · ~Week 4

### Customer Khata (udhaar)
- [ ] Khata list (`customer_khata`) — total lena hai, filter (All/Overdue/Recent), per-customer balance, 30+ din tag
- [ ] Add / edit customer
- [ ] Customer detail (`customer_ramesh_sharma`) — Khata / Bills / Dawai tabs
  - [ ] Ledger with running balance, record payment
  - [ ] Dawai tab: chronic-medicine refill prediction
  - [ ] WhatsApp reminder

### Expiry management
- [ ] Expiry screen (`expiry_management`) — 30/60/90-day tabs with counts, value-at-risk card
- [ ] Grouped **by supplier** (returns go supplier-wise), select items, return-window status
- [ ] "Return Note banao" → Credit Note draft → supplier ledger
- [ ] Near-expiry highlight already surfaced in billing (FEFO)

### Reports & Day Close
- [ ] Reports screen (`reports_day_close`)
  - [ ] Day close: expected vs actual cash, shortfall indicator
  - [ ] Stat tiles: sale, profit (on landed cost), bills count, avg bill
  - [ ] Last 7 days bar chart
  - [ ] Fast movers / Dead stock (90d) / Missed sales / GST summary (GSTR-1) / item-wise profit
  - [ ] Share report (PDF/WhatsApp)

---

## 6. PHASE 4 — Voice Layer (the differentiator) · ~Week 5

- [ ] STT integration (SpeechRecognizer, hi-IN) + permission flow
- [ ] TTS integration (speak confirmations, totals, query answers)
- [ ] **Fuzzy matching layer** — STT text → medicine master (phonetic + Levenshtein; handle "Dolo/Dollo/Dulo")
- [ ] Hinglish number parsing ("do", "dhai sau", "sava sau", mixed Hindi/English)
- [ ] Voice billing: "Dolo 650 do strip, Pan D ek strip" → parse → confirm sheet
- [ ] Voice confirm sheet (`voice_assistant_overlay`) — transcript, parsed cards, "Confirm karo" chips for uncertain items, TTS read-back
- [ ] Voice command centre (`voice_command_centre`) — long-press mic, full-screen
  - [ ] Queries: "aaj ka collection?", "Cipla ka payment kitna baaki?", "Dolo ka stock?", "naya bill banao", "kaunsi dawai expire ho rahi hai?"
  - [ ] Answer card + TTS
- [ ] Voice purchase entry: "Batch A123, expiry 08/27, qty 50"
- [ ] Voice fill in Add Medicine
- [ ] Settings: language (Hinglish/Hindi/English), voice-confirm on/off, speech speed
- [ ] Noisy-environment fallback: "Type karo"

---

## 7. PHASE 5 — Barcode, WhatsApp, PO, Refill · ~Week 6

- [ ] Barcode scan (ML Kit + CameraX) in billing + purchase + add-medicine
  - [ ] Map barcode → medicine; learn new barcodes on first scan
- [ ] Missed Sale register (`missed_sale_register`) — one-tap log, times-asked, "PO mein daalo"
- [ ] Purchase Order generator (`purchase_order_generator`)
  - [ ] Auto-suggest from reorder level + fast movers + missed sales
  - [ ] Editable qty, supplier select, WhatsApp/PDF send
- [ ] Refill reminders — chronic customers, predicted run-out, WhatsApp/local notification
- [ ] WhatsApp deep-links everywhere (bill, statement, reminder, PO)
- [ ] Local notifications (PWA-style) for expiry/reorder/refill/payment-due

---

## 8. PHASE 6 — Cloud Sync & Backup (multi-device / safety) · when needed

- [ ] v1 backup: export full DB to JSON/SQLite → Google Drive / Downloads (manual + scheduled)
- [ ] Import/restore from backup
- [ ] Supabase project (free) — Postgres schema mirroring Room entities
- [ ] Background write-through sync (Room = source of truth, push on connectivity)
- [ ] Conflict handling (updatedAt / last-write-wins per row; soft deletes)
- [ ] Sync status UI (Settings: "Last sync: 2 min pehle")
- [ ] Supabase Auth (phone OTP) — only when multi-device/multi-store needed

---

## 9. Compliance (India-specific — don't skip)

- [ ] Schedule H1 register (`ScheduleH1Register`) — patient + doctor name, qty, date, Rx photo; 3-yr retention
- [ ] Prompt for Rx on H1 drugs at billing
- [ ] GST: correct slabs (5/12/18), HSN on items, GSTR-1 style monthly summary
- [ ] Drug license no + GSTIN on every printed bill
- [ ] Data retention & export for audit

---

## 10. Multi-store Readiness (future — kept cheap now)

- [ ] `storeId` on every entity from day 1 (single store = one id now)
- [ ] Repositories filter by active store
- [ ] Later: store switcher, per-store reports, Supabase Row-Level-Security by storeId
- [ ] Later: staff roles (owner/staff), staff-wise sale tracking

---

## 11. Testing & Quality

- [ ] Unit tests: landed-cost calc, FEFO selection, GST math, ledger running balance, loose-unit conversion
- [ ] Instrumented tests: billing flow, purchase flow, stock decrement
- [ ] Voice parsing test set (Hinglish samples, medicine phonetics)
- [ ] Offline test: airplane mode — billing/stock/khata all work
- [ ] Edge cases: negative stock guard, expired-batch block, split payment, partial supplier payment
- [ ] Manual QA on a real cheap Android device at "counter speed"

---

## 12. Release

- [ ] App icon, screenshots, store listing copy (Hinglish)
- [ ] Signed release build (APK for direct install first — ₹0; Play Store ₹2000 one-time later)
- [ ] ProGuard/R8, crash reporting (free: Firebase Crashlytics or Sentry free tier)
- [ ] Onboarding: first-run shop profile + import stock (Excel/one-by-one)
- [ ] Backup reminder on first run

---

## Screen → Feature Map (design ↔ build)

| Design screen | Phase | Feature |
|---|---|---|
| home_dashboard | 1 | Dashboard, alerts, quick actions |
| new_sale_billing | 1 | Billing/POS |
| item_picker | 1 | Batch/FEFO/substitute |
| stock_list | 1 | Inventory |
| item_detail | 1 | Item + batches |
| add_edit_medicine | 1 | Item master |
| suppliers_payments | 2 | Suppliers + dues |
| purchase_entry | 2 | Purchase + landed cost |
| supplier_ledger | 2 | Supplier ledger |
| customer_khata | 3 | Customer udhaar |
| customer_ramesh_sharma | 3 | Customer detail + refill |
| expiry_management | 3 | Expiry + returns |
| reports_day_close | 3 | Reports + day close |
| voice_assistant_overlay | 4 | Voice bill confirm |
| voice_command_centre | 4 | Voice queries |
| missed_sale_register | 5 | Missed sales |
| purchase_order_generator | 5 | PO generator |
| more_menu_hub | 1 | More menu (nav) |
| settings_more | 4/6 | Settings/voice/backup |

---

## Open decisions / notes

- [ ] Final app name usage: "Patta" only on splash/login; shop name in all headers
- [ ] Fix pending in designs: customer_khata header still says "PharmaAssist" → change to shop name
- [ ] Minor design fixes (cosmetic): Missed Sales "+ Note" button overflow, Add Medicine save-button edge
- [ ] Decide: ship offline-only v1, add Supabase sync in Phase 6 (recommended)
