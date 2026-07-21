# Pharmacy App — Stitch Design Prompts

Paste the MASTER STYLE block at the top of every screen prompt (or set it as the
project-level style in Stitch), then paste one screen prompt at a time.

---

## 0. MASTER STYLE PROMPT (prefix for every screen)

Design a mobile app screen for an Indian retail medical/pharmacy shop owner.
The user is a shopkeeper standing at a busy counter, often with one hand free,
sometimes not looking at the screen. Design language:

- Mobile-first, 390x844, single column, one-thumb reachable.
- Clean, calm, high-contrast. Light background (#F7F8FA), white cards, soft
  rounded corners (16px), subtle shadows. Primary color: medical teal (#0E9F8C).
  Accent green for money-in (#16A34A), amber for warnings/expiry (#F59E0B),
  red for money-out/overdue (#DC2626).
- Large typography. Numbers (amounts, quantities, stock) are the biggest and
  boldest elements on every screen — a shopkeeper should read them from arm's
  length. Currency always in ₹ with Indian comma format (₹1,24,500).
- Bottom navigation bar with 5 items: Home, Billing, Stock, Khata, More.
- A persistent circular VOICE MIC button floating above the bottom nav (right
  side), teal, with a subtle pulse ring. It is the primary interaction of the app.
- Minimal typing. Prefer big tappable chips, steppers (+/-), and quick-action
  cards over text fields and dropdowns.
- Labels in simple Hinglish/English mix as an Indian shopkeeper would say them
  (e.g. "Udhaar", "Khata", "Aaj ki Sale", "Supplier ko dena hai").
- No clutter, no decorative illustrations. Every pixel is information or action.

---

## 1. HOME / DASHBOARD

Screen: Home dashboard.
- Top bar: shop name "Sharma Medical Store", small drug-license number below it,
  a bell icon with a red badge count on the right.
- A big hero card: "Aaj ki Sale" with a very large amount (₹18,240), and below it
  three small pills: Cash ₹9,100 · UPI ₹6,340 · Udhaar ₹2,800.
- A row of 2 alert cards, horizontally scrollable, colour-coded:
  - Amber card: "12 dawai 60 din mein expire" → "Dekho".
  - Red card: "₹47,300 supplier ko dena hai (3 bill)" → "Dekho".
- A 2x2 grid of large quick-action tiles with icons: "Naya Bill", "Purchase Entry",
  "Khata / Udhaar", "Stock Check".
- A "Kam stock" section: a compact list of 3 items showing item name, salt name in
  smaller grey text, and remaining quantity in red (e.g. "Dolo 650 — 2 strip left"),
  each row has a small "+ PO" button.
- Bottom nav + floating voice mic button.

---

## 2. BILLING — NEW SALE (core screen)

Screen: New Sale / Billing.
- Top: a large search bar "Dawai ka naam bolo ya likho" with a mic icon inside it
  and a barcode-scan icon on the right.
- Below search: the current bill as a list of added items. Each row shows:
  item name (bold), batch number + expiry in small grey text, an amber dot if the
  batch is near expiry, a quantity stepper (– 2 +) with "strip" unit toggle
  (Strip / Tablet), and the line amount right-aligned in bold.
- Swipe-left on a row reveals a red delete action.
- A sticky bottom summary sheet above the nav bar: subtotal, GST, discount (with a
  small "Add discount" link), and a very large TOTAL. Below it, three large payment
  buttons side by side: "CASH" (green), "UPI" (blue), "UDHAAR" (amber).
- Empty state (when no items): a big friendly mic illustration with the text
  "Mic dabao aur bolo — 'Dolo 650 do strip'".
- Bottom nav + floating voice mic button (mic is enlarged and glowing on this screen).

---

## 3. VOICE LISTENING + CONFIRMATION SHEET

Screen: Voice input overlay on top of the Billing screen.
- The screen is dimmed. A large bottom sheet slides up.
- Top of sheet: an animated waveform with the text "Sun raha hoon..." while
  listening.
- After recognition, show the transcribed sentence in large text:
  "Dolo 650 do strip aur Pan D ek strip" — with the recognised medicine names
  highlighted in teal.
- Below it, the parsed result as confirmable cards, one per item:
  each card shows medicine name, matched batch + expiry, quantity stepper, price,
  and a small "galat hai?" link that opens alternatives.
- If a name is uncertain, show that card in amber with the label "Confirm karo"
  and 2–3 suggestion chips of similar medicine names to tap.
- Bottom of sheet: two big buttons — "Bill mein daalo" (teal, full width) and
  "Dobara bolo" (outlined).
- A small speaker icon indicating the app will read the total back aloud.

---

## 4. ITEM / BATCH PICKER

Screen: Item search results and batch selection.
- Search field at top with the typed/spoken query.
- Results list: each result card shows brand name (bold), salt/composition below in
  grey, company name, MRP, and a green "In stock: 14 strip" or red "Out of stock".
- Tapping a result expands to show BATCH CHIPS, sorted by earliest expiry first:
  each chip shows "B-A123 · Exp 08/27 · 12 strip · ₹32". The earliest-expiring batch
  is marked with a teal "Ye pehle becho" tag and pre-selected.
- If an item is out of stock, show a highlighted section "Same salt available:"
  listing alternative brands in stock, each with its margin shown as a small
  green percentage badge.
- A footer button "Missed Sale note karo" for when nothing suitable is available.

---

## 5. STOCK / INVENTORY LIST

Screen: Stock list.
- Top: search bar with mic + barcode icon.
- Filter chips row (horizontally scrollable): "Sab", "Kam stock", "Near expiry",
  "Dead stock", "Fridge".
- List of items grouped alphabetically. Each row: item name bold, salt in grey,
  total quantity on the right in large numbers with unit ("14 strip"), and a thin
  coloured bar underneath indicating stock health (green/amber/red).
- Near-expiry items show a small amber "Exp 60d" tag.
- Floating "+" button to add a new item.
- Bottom nav + voice mic.

---

## 6. ITEM DETAIL

Screen: Item detail for "Dolo 650 (Paracetamol 650mg) — Micro Labs".
- Header card: name, salt, company, pack size (1x15), HSN, GST 12%, rack location
  "Rack 3 · Shelf B".
- Three stat tiles in a row: Total Stock (bold number), Landed Cost, Margin %
  (green).
- "Batches" section: a table-like list of cards, each showing batch no, expiry date
  (colour-coded: red <30d, amber <90d, green otherwise), quantity, MRP, purchase
  rate.
- "Movement" section: last 30 days sold quantity as a small sparkline, and
  reorder-level setting with a stepper.
- Bottom action bar: "Bill mein daalo" and "PO banao".

---

## 7. PURCHASE ENTRY (supplier invoice)

Screen: Purchase entry — step-based, minimal typing.
- Step indicator at top: 1 Supplier → 2 Items → 3 Confirm.
- Step 2 shown: supplier name chip at top ("Cipla Distributors · Invoice #4821 ·
  Due 30 din").
- A prominent "Bolke entry karo" banner card with a mic icon: "Batch, expiry, qty
  bol do — main bhar dunga".
- Item entry rows already added: each row shows item name, batch, expiry, free qty
  (e.g. "10 + 1 free"), rate, scheme discount, and the computed LANDED COST in teal
  with the margin % in green next to it.
- Add-item row at the bottom with inline fields: Item (search+mic), Batch, Expiry
  (month-year picker), Qty, Free, Rate, Disc%.
- Sticky footer: invoice total, GST, and a large "Save Purchase" button.
- A subtle warning banner if invoice total does not match the sum of items:
  "Bill total match nahi ho raha — ₹120 ka fark".

---

## 8. SUPPLIERS + PAYMENTS DUE

Screen: Suppliers.
- Top hero strip: "Total dena hai" with a big red amount ₹47,300, and below it
  "Is hafte: ₹18,400".
- A horizontal "Payment calendar" strip: 7 day chips (Mon–Sun) with a red dot and
  amount under days that have a payment due.
- List of suppliers: each card shows supplier name, phone icon, outstanding balance
  in bold red, and the next due date as a small chip. Overdue ones have a red left
  border and an "Overdue 3 din" tag.
- Each card has two small buttons: "Payment karo" and "Ledger".
- Floating "+" to add a supplier.

---

## 9. SUPPLIER LEDGER

Screen: Ledger for "Cipla Distributors".
- Header: supplier name, phone, credit period "30 din", and a big outstanding
  balance card in red with a "Payment karo" button.
- A running statement list, newest first. Each entry is a row with date on the left,
  description in the middle, and amount on the right —
  purchases in red (+₹12,400 · Invoice #4821), payments in green (–₹10,000 · UPI),
  credit notes in blue (–₹1,850 · Expiry return).
  A running balance is shown in grey under each row.
- A filter chip row at top: "Sab", "Bills", "Payments", "Returns".
- Bottom: "Statement share karo (WhatsApp)" button.

---

## 10. KHATA / CUSTOMER CREDIT

Screen: Khata (customer udhaar).
- Top hero: "Lena hai" big green amount ₹32,150 across 24 customers.
- Search bar with mic.
- Customer list: avatar with initials, name, phone, last transaction date in grey,
  and outstanding amount on the right in bold. Overdue > 30 days shows an amber
  "30+ din" tag.
- Each row swipes left to reveal "Yaad dilao" (WhatsApp reminder) and
  "Payment mila".
- Floating "+" to add customer.

---

## 11. CUSTOMER DETAIL / LEDGER

Screen: Customer "Ramesh Sharma".
- Header: name, phone with call + WhatsApp icons, outstanding balance in a large
  amber card with a "Payment mila" button.
- Tabs: "Khata" | "Bills" | "Dawai".
- Khata tab: dated entries, bills in red, payments in green, running balance.
- Dawai tab: chronic medicines this customer buys with a refill prediction —
  "Telma 40 — agli dawai 12 Aug tak khatam" with a "Yaad dilao" button.
- Bottom: "Reminder bhejo (WhatsApp)".

---

## 12. EXPIRY MANAGEMENT

Screen: Expiry.
- Three big tab chips at top with counts: "30 din (4)" red, "60 din (12)" amber,
  "90 din (27)" yellow.
- Below: a total value at risk card — "Risk pe: ₹18,400 ka stock".
- List grouped BY SUPPLIER (because returns go back supplier-wise). Each supplier
  group header shows supplier name and total returnable value, with a checkbox to
  select all.
- Each item row: checkbox, item name, batch, expiry date, quantity, value, and a
  small tag showing "Return window: 45 din baaki" or a red "Return window closed".
- Sticky bottom bar when items are selected: "3 item selected · ₹4,200" with a
  large button "Return Note banao".

---

## 13. REPORTS / DAY CLOSE

Screen: Reports & Day Close.
- Date selector at top (Aaj / Kal / This month / Custom).
- "Din ka hisaab" card: expected cash in drawer (₹9,100) with an input for actual
  counted cash and an instant match/mismatch indicator (green tick or red
  "₹200 kam hai").
- Stat tiles grid: Sale, Profit (in green, computed on landed cost), Bills count,
  Avg bill value.
- A simple bar chart of last 7 days' sales.
- Sections list with chevrons: "Fast movers", "Dead stock (90 din se nahi bika)",
  "Missed sales", "GST summary (GSTR-1)", "Item-wise profit".
- Bottom: "Report share karo (PDF / WhatsApp)".

---

## 14. VOICE COMMAND CENTRE (full-screen assistant)

Screen: Voice assistant, opened by long-pressing the mic.
- Full-screen, dark teal gradient background, calm.
- Center: a large animated listening orb.
- Above it, the live transcript in large white text.
- Below, "Aap ye bol sakte ho:" with example command chips —
  "Aaj ka collection?", "Cipla ka payment kitna baaki?",
  "Dolo ka stock?", "Naya bill banao", "Kaunsi dawai expire ho rahi hai?"
- When answering, show a result card with the answer in very large numbers and a
  speaker icon indicating it is being read aloud.
- A small "Type karo" option at the bottom for noisy environments.

---

## 15. SETTINGS / MORE

Screen: More.
- Shop profile card: shop name, drug license no, GSTIN, address, logo.
- List sections with icons:
  - Shop: Staff & roles, Rack locations, Printer setup
  - Data: Backup & sync status ("Last sync: 2 min pehle"), Export to Excel, Import stock
  - Voice: Language (Hinglish / Hindi / English), Voice confirmation on/off,
    Speech speed
  - Compliance: Schedule H1 register, GST settings
  - App: Offline mode indicator, About
- A visible offline/online status pill at the top ("Offline — sab kaam chal raha hai").
