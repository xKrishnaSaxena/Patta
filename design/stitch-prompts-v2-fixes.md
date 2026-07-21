# Pharmacy App — Stitch Fixes + New Screens (v2)

How to use this file:
1. Paste BLOCK A (Global Correction Rules) into Stitch as the project-level style /
   system prompt, replacing or appending to the old master style. This fixes the
   systemic issues (mic overlap, header name, nav highlight) for all future gens.
2. Re-generate ONLY the broken screens listed in BLOCK B, pasting BLOCK A + the
   per-screen fix note.
3. Generate the 4 new screens in BLOCK C (More menu + 3 missing screens).

===============================================================================
## BLOCK A — GLOBAL CORRECTION RULES (set as project style)
===============================================================================

Apply these rules to EVERY screen of this Indian pharmacy shopkeeper app. They
override any conflicting earlier instruction.

FLOATING BUTTON RULES (strict — this fixes the biggest problem):
- NEVER place two floating buttons in the same corner. Never stack a voice mic and
  a "+" button on top of each other. Never let any floating button overlap a card,
  a list row, or a bottom action bar.
- Decide the bottom-right element per screen using this priority:
  1. If the screen has a "+" add button (add item / add supplier / add customer),
     the "+" owns the bottom-right corner. There is NO floating mic on this screen.
     Voice is available as a small mic icon INSIDE the search bar instead.
  2. If the screen has a sticky bottom action bar (payment buttons, "Return Note
     banao", "Save Purchase", "Bill mein daalo", "Reminder bhejo", "Report share"),
     there is NO floating mic. Voice is a small mic icon inside the top search bar
     (or, if no search bar, a mic icon on the LEFT end of the bottom action bar).
     Nothing floats over the action bar.
  3. Only if the screen has neither a "+" button nor a bottom action bar, show the
     circular teal voice mic FAB (64px) in the bottom-right, 24px from edges, with
     a soft pulse ring.
- Every scrollable list has 96px of bottom padding so the last row is never hidden
  behind the bottom nav, a FAB, or an action bar.

BOTTOM NAV (5 tabs, fixed): Home · Billing · Stock · Khata · More.
- The active tab must match the current section. Screens reached THROUGH the "More"
  menu — Suppliers, Payments, Purchase Entry, Expiry Management, Reports, Settings —
  all keep the "More" tab highlighted (not Stock or Khata).
- "Khata" tab = CUSTOMER udhaar only. Supplier payments live under More, never under
  the Khata tab.

Everything else (medical teal #00685b primary, green money-in, amber expiry, red
money-out, big numbers, white cards on soft grey, Hinglish labels, Plus Jakarta
Sans for numbers) stays as before.

===============================================================================
## BLOCK B — PER-SCREEN FIX NOTES (regenerate these screens)
===============================================================================

For each, paste BLOCK A first, then the original screen prompt, then this fix note.

[B1] NEW SALE / BILLING — fix note:
"Remove the floating mic button entirely from this screen. The three payment
buttons CASH / UPI / UDHAAR must be fully visible, equal width, with nothing
overlapping UDHAAR. Voice input is the mic icon already inside the top search bar.
Add 96px bottom padding above the summary sheet."

[B2] STOCK LIST — fix note:
"Only the '+' add-item button floats in the bottom-right. Remove the floating voice
mic; voice is the mic icon inside the search bar. The '+' button must sit clear of
the last list row and the bottom nav (add 96px list bottom padding). Nothing overlaps
the Calpol row."

[B3] SUPPLIERS + PAYMENTS DUE — fix note:
"Only the '+' add-supplier button floats bottom-right, and it must not cover any
card's 'Payment karo' button — add 96px bottom padding so the last supplier card is
fully visible above it. Remove any floating mic. The active bottom-nav tab is 'More',
not 'Khata'."

[B4] CUSTOMER KHATA / UDHAAR — fix note:
"Only the '+' add-customer button floats bottom-right; remove the floating voice mic
(voice is the mic in the search bar). Ensure the last customer card is fully visible
above the '+' button (96px bottom padding). Active tab is 'Khata'."

[B5] EXPIRY MANAGEMENT — fix note:
"Remove the floating mic. The sticky bottom bar 'Return Note banao' spans the width
with nothing overlapping it. If voice is needed, show a small mic icon on the LEFT
end of that bottom bar. Active bottom-nav tab is 'More', not 'Stock'."

[B6] ITEM DETAIL — fix note:
"Remove the floating mic peeking behind the bottom nav. This screen has a bottom
action bar ('Bill mein daalo' / 'PO banao') — nothing floats over it. Active tab is
'More' (reached via Stock item, but keep nav consistent — highlight 'Stock')."

[B7] REPORTS / DAY CLOSE — fix note:
"Remove the floating mic overlapping the 'Report share karo' bar. Active bottom-nav
tab is 'More', not 'More'-as-current is fine. The share bar spans full width, nothing
over it."

[B8] SUPPLIER LEDGER — fix note:
"Header shows the shop name 'Sharma Medical Store', not any app name. Remove the mic
peeking behind the bottom nav. Active tab is 'More', not 'Khata'."

[B9] PURCHASE ENTRY — fix note:
"The floating mic must not overlap the 'Add Item' card or the bottom summary. Since
this screen has a bottom 'Save Purchase' bar and an in-form mic, remove the floating
mic. Active bottom-nav tab is 'More', not 'Stock'."

===============================================================================
## BLOCK C — NEW SCREENS (generate fresh)
===============================================================================

Paste BLOCK A before each of these.

-------------------------------------------------------------------------------
[C0] MORE — MENU HUB (new, this is the 5th nav tab's screen)
-------------------------------------------------------------------------------
Screen: "More" menu hub.
- Header: shop name "Sharma Medical Store" with drug-license number below.
- A grid or list of large, icon-led menu entries, grouped in sections:
  - "Paisa & Supplier": "Suppliers & Payments" (with a small red badge "₹47,300
    dena hai"), "Purchase Entry", "Purchase Orders".
  - "Stock": "Expiry Management" (amber badge "12 near expiry"), "Missed Sales",
    "Add / Edit Medicine".
  - "Hisaab": "Reports & Day Close", "GST Summary".
  - "Settings": "Shop Profile", "Staff & Roles", "Backup & Sync", "Voice & Language".
- Each entry is a big tappable row: icon in a soft tinted circle on the left, title,
  a one-line grey subtitle, a chevron on the right, and an optional coloured badge.
- No floating mic (this screen has none of the bottom conflicts, but keep it clean;
  a small mic is fine in a top search bar if shown).
- Bottom nav with "More" active.

-------------------------------------------------------------------------------
[C1] MISSED SALE REGISTER (new)
-------------------------------------------------------------------------------
Screen: Missed Sales — medicines customers asked for but were out of stock.
- Header: "Missed Sales" with a small subtitle "Jo maanga, nahi tha".
- A top card: "Is hafte 14 baar stock nahi tha" with a small line "Inko order karo".
- A prominent add row at top: a search bar with a mic icon "Kya maanga tha? bolo ya
  likho" and a big "+ Note karo" button — one tap logs a missed item.
- List of missed items, newest first. Each row: medicine name (bold), salt in grey,
  how many times asked ("3 baar" as a small chip), last asked date, and on the right
  a green "PO mein daalo" button.
- Selectable rows with checkboxes; a sticky bottom bar "5 selected → Purchase Order
  banao" (teal) when any are selected.
- Empty state: friendly text "Abhi tak koi missed sale nahi — badhiya!".
- Bottom nav with "More" active. No floating mic (search-bar mic covers voice).

-------------------------------------------------------------------------------
[C2] ADD / EDIT MEDICINE (new — item master form)
-------------------------------------------------------------------------------
Screen: Add / Edit Medicine — a clean form with minimal typing.
- Header: "Nayi Dawai" with a back arrow and a "Save" text button top-right.
- A big banner card at top: mic icon "Bolke bhar do — 'Dolo 650, Micro Labs, 12% GST'"
  so fields can be filled by voice.
- Form fields as large, well-spaced inputs, grouped:
  - Identity: Medicine name (search-as-you-type with mic), Salt / Composition
    (with an auto-suggest chip row), Company / Manufacturer.
  - Pack: Pack type toggle chips (Strip / Bottle / Box / Tube), Units per pack
    stepper (e.g. 1 x 15), a "Loose sale allowed?" toggle.
  - Pricing & tax: MRP, Purchase Rate, GST% as chips (0 / 5 / 12 / 18), HSN code.
  - Shelf: Rack location picker ("Rack 3 · Shelf B"), Reorder level stepper.
  - Flags: toggles for "Schedule H1 (prescription needed)", "Fridge item".
- Fields the app can auto-learn from purchase bills show a small grey hint
  "purchase se auto bhar jayega".
- Sticky bottom: full-width teal "Save Medicine" button. No floating mic.
- Bottom nav with "More" active.

-------------------------------------------------------------------------------
[C3] PURCHASE ORDER GENERATOR (new)
-------------------------------------------------------------------------------
Screen: Create Purchase Order — auto-suggested reorder list to send a supplier.
- Header: "Purchase Order" with a supplier selector chip at top
  ("Cipla Distributors ▾").
- A summary strip: "24 items suggest kiye — ₹32,400 ka order".
- Tabs or filter chips: "Auto-suggested", "Low stock", "Missed sales", "Fast movers".
- List of suggested items, each a row with: checkbox (pre-checked), medicine name,
  salt in grey, a reason tag ("2 strip left" red / "14 baar maanga" / "fast mover"),
  current stock, and an editable order-quantity stepper on the right.
- Ability to add an item manually via a search+mic row at the bottom of the list.
- Sticky bottom bar: total items + estimated value, and two buttons: "WhatsApp bhejo"
  (green) and "PDF banao" (outline).
- Bottom nav with "More" active. No floating mic (search-bar mic covers voice).
