# Patta — End-to-End Test Guide

App: `frontend/` (open in Android Studio → Run on phone/emulator).
Everything works offline (Room). Money is stored in paise; GST is inclusive of MRP.

## What's built (Phase 1–3 + polish)

| Area | Screen | Status |
|---|---|---|
| Dashboard | Home | ✅ Aaj ki sale, alerts, quick tiles, kam-stock |
| Billing | Billing (POS) | ✅ search, FEFO batch, cart, strip/tab loose sale, Cash/UPI/Udhaar, PDF share |
| Inventory | Stock list | ✅ groups, filters, stock-health bar; tap = edit |
| Master | Add/Edit Medicine | ✅ add + edit, opening stock |
| Suppliers | Suppliers, Purchase Entry, Supplier Ledger | ✅ landed cost, restock picker, ledger, payments |
| Khata | Customers, Customer Ledger | ✅ udhaar linked to bills, payments |
| Expiry | Expiry Management | ✅ 30/60/90 tabs, supplier return → credit note |
| Reports | Reports & Day Close | ✅ sale/profit/bills/avg, cash match, GST |
| Return | Sale Return, Bill Return | ✅ stock back + udhaar reversal |

Not built yet: voice (Phase 4), missed-sale register, PO generator, barcode, refill reminders, Supabase sync.

## Step-by-step test (~10 min, full loop)

### 1. Add a supplier + purchase (stock enters via real purchase)
1. **More → Suppliers & Payments → "+"** → Cipla, phone, credit 30 din → Save.
2. Tap **Cipla → "Naya Purchase"**. Invoice No: `INV-1`.
3. Add item: Name `Dolo 650`, Salt `Paracetamol`, Batch `A123`, Exp `08/27`,
   Qty `10`, Free `1`, Rate `18`, Scheme `5`, GST `12`, MRP `30` → **Add Item**.
   - Card shows **Landed cost** + **Margin %** (real, after scheme + free).
4. Add a 2nd item: `Pan D`, Batch `P55`, Exp `06/26`, Qty `5`, Rate `80`, GST `12`, MRP `120`.
   - **Restock proof:** type `Dolo` again as name → suggestion appears → tap → auto-fills.
5. **Save Purchase**. → Cipla ledger shows a `+` entry, outstanding rises.

### 2. Check stock + edit
6. **Stock tab** → Dolo shows **11** (10+1 free), Pan D **5**.
7. Tap **Dolo** → edit screen (prefilled). Turn on **"Loose sale allowed"**, set Units/pack `15`, Rack `A3` → Save.

### 3. Billing — cash, loose sale, PDF
8. **Billing tab** → search `Dolo` → tap → in cart.
9. Toggle **Tablet** on the Dolo line → price becomes per-tablet (₹30/15 = ₹2). Set qty `4`.
10. Search `Pan` → add, qty `1`. → Total updates (GST shown as included).
11. Tap **CASH** → dialog "Bill INV-1 bana ✅" → **Bhejo (PDF/WhatsApp)** → share sheet opens (pick WhatsApp/Files).
12. **Stock** → Dolo now ~**10.7** (4 tablets = 0.27 strip gone), Pan D **4**.

### 4. Billing — udhaar linked to khata
13. Billing → add Pan D → tap **UDHAAR** → "Udhaar kiske naam?" → type `Ramesh` → **Naya + Udhaar**.
14. **Khata tab** → "Lena hai" shows the amount; Ramesh card visible.
15. Tap **Ramesh** → statement has "Bill INV-2 +₹…". Tap **Payment mila** → enter amount → balance drops, "Payment −₹…" appears.

### 5. Sale return
16. **More → Sale Return** → pick a bill → **Bill Return** → tap **Return** on a line.
17. Stock for that item goes back up (Stock tab). If it was an udhaar bill, Ramesh's khata drops by the returned value.

### 6. Home + Reports + Expiry
18. **Home** → "Aaj ki Sale" with Cash/UPI/Udhaar split; kam-stock + near-expiry alerts.
19. **More → Reports** → Sale, Est. Profit (on landed cost), Bills, Avg. Enter actual cash → match/shortfall. GST collected today.
20. **More → Expiry Management** → Pan D (Exp 06/26) shows under a tab; grouped by supplier; **Return** → stock zeroed + Cipla gets a credit note (ledger `−`, outstanding drops).

## Notes / known limits
- Bill PDF shop name/GSTIN are placeholder constants (Shop Profile screen not built).
- Sale return is whole-remaining per line (no partial-qty picker yet).
- New medicine added via Purchase gets minimal fields — set pack size/rack later via Stock → edit.
- First install after a schema change wipes local data (destructive migration, dev only).
