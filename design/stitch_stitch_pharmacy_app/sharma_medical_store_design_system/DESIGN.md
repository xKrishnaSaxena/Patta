---
name: Sharma Medical Store Design System
colors:
  surface: '#f8f9ff'
  surface-dim: '#cbdbf5'
  surface-bright: '#f8f9ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#eff4ff'
  surface-container: '#e5eeff'
  surface-container-high: '#dce9ff'
  surface-container-highest: '#d3e4fe'
  on-surface: '#0b1c30'
  on-surface-variant: '#3d4946'
  inverse-surface: '#213145'
  inverse-on-surface: '#eaf1ff'
  outline: '#6d7a76'
  outline-variant: '#bcc9c5'
  surface-tint: '#006b5d'
  primary: '#00685b'
  on-primary: '#ffffff'
  primary-container: '#008373'
  on-primary-container: '#f4fffb'
  inverse-primary: '#63dac5'
  secondary: '#006e2d'
  on-secondary: '#ffffff'
  secondary-container: '#7cf994'
  on-secondary-container: '#007230'
  tertiary: '#825100'
  on-tertiary: '#ffffff'
  tertiary-container: '#a36700'
  on-tertiary-container: '#fffbff'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#81f6e0'
  primary-fixed-dim: '#63dac5'
  on-primary-fixed: '#00201b'
  on-primary-fixed-variant: '#005046'
  secondary-fixed: '#7ffc97'
  secondary-fixed-dim: '#62df7d'
  on-secondary-fixed: '#002109'
  on-secondary-fixed-variant: '#005320'
  tertiary-fixed: '#ffddb8'
  tertiary-fixed-dim: '#ffb95f'
  on-tertiary-fixed: '#2a1700'
  on-tertiary-fixed-variant: '#653e00'
  background: '#f8f9ff'
  on-background: '#0b1c30'
  surface-variant: '#d3e4fe'
typography:
  display-currency:
    fontFamily: Plus Jakarta Sans
    fontSize: 36px
    fontWeight: '700'
    lineHeight: 44px
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 24px
    fontWeight: '700'
    lineHeight: 32px
  headline-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 28px
  body-lg:
    fontFamily: Inter
    fontSize: 18px
    fontWeight: '500'
    lineHeight: 26px
  body-md:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  label-hinglish:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '600'
    lineHeight: 20px
  headline-lg-mobile:
    fontFamily: Plus Jakarta Sans
    fontSize: 22px
    fontWeight: '700'
    lineHeight: 30px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  base: 4px
  xs: 8px
  sm: 12px
  md: 16px
  lg: 24px
  xl: 32px
  safe-bottom: 80px
---

## Brand & Style
The design system is engineered for the high-velocity environment of an Indian retail pharmacy. The brand personality is rooted in **Reliability, Speed, and Medical Authority**. It prioritizes function over form, ensuring that a shopkeeper can manage inventory and billing amidst the chaos of a busy storefront.

The visual style is **Corporate Modern with a Functional Utility** twist. It utilizes a high-contrast interface with generous tap targets to facilitate "one-thumb" operation. The aesthetic is clean and surgical, removing all decorative clutter to focus entirely on transactional data and inventory status.

**Key Principles:**
- **At-a-Glance Legibility:** Critical data points (stock levels, total amounts) must be readable at arm's length.
- **Trust through Clarity:** Use of standardized medical colors to signal safety and urgency.
- **Operational Efficiency:** UI patterns optimized for one-handed use, with primary actions anchored to the bottom of the screen.

## Colors
This palette uses "Medical Teal" as the primary anchor to establish a professional healthcare identity. The supporting semantic colors are strictly tied to business outcomes:

- **Primary (Medical Teal):** Used for branding, primary buttons, and the floating voice interface.
- **Success (Green):** Specifically reserved for "Money-In" transactions, completed sales, and healthy stock levels.
- **Warning (Amber):** Used for near-expiry alerts and low-stock warnings.
- **Error (Red):** Used for "Money-Out" (expenses), expired stock, and critical system errors.
- **Neutral/Background:** A soft grey background creates a low-glare environment, allowing the pure white surfaces (cards) to pop, indicating interactable areas.

## Typography
The typography system uses **Plus Jakarta Sans** for headlines and large numeric displays to provide a modern, highly legible feel. **Inter** is used for all body text and UI labels due to its exceptional readability in dense data environments.

**Currency Formatting:**
- Use the ₹ symbol with the Indian numbering system (e.g., ₹1,00,000).
- Numbers representing financial totals should use the `display-currency` token to ensure they are readable even when the phone is resting on a counter.

**Hinglish Labels:**
- Use the `label-hinglish` style for localized terms like *Udhaar* (Credit), *Khata* (Ledger), and *Galla* (Cashbox). These labels should be semi-bold to distinguish them from standard English UI descriptors.

## Layout & Spacing
The layout follows a **Fluid Grid** model with a focus on vertical stackability. 

- **Margins:** A consistent 16px (md) margin is applied to the left and right of all screens.
- **Gutter:** 12px (sm) between cards in a list.
- **One-Thumb Reachability:** All primary interactive elements, including the 5-item bottom navigation and the Floating Action Button (FAB), are placed within the bottom 33% of the screen.
- **Safe Areas:** A `safe-bottom` padding of 80px is maintained at the end of all scrollable lists to ensure the floating voice mic button does not obscure critical information or last-item actions.

## Elevation & Depth
The design system utilizes **Tonal Layers** rather than heavy shadows to maintain a clean, medical aesthetic. 

- **Level 0 (Background):** Soft Grey (#F7F8FA) acts as the canvas.
- **Level 1 (Cards):** Pure White (#FFFFFF) surfaces with a subtle 1px border (#E2E8F0) and no shadow.
- **Level 2 (Active/Modals):** A very soft, diffused shadow (0px 4px 12px rgba(0, 0, 0, 0.05)) is used only for elevated components like the bottom sheet or the persistent Voice Mic button.
- **Interactive State:** When a card is pressed, it should subtly scale down (98%) rather than changing color, providing tactile feedback without visual noise.

## Shapes
The shape language is defined by friendly but structured containers.
- **Cards & Primary Containers:** Use 16px (1rem) rounded corners to create a modern, approachable feel that reduces the "sharpness" of medical data.
- **Buttons:** 12px (0.75rem) roundedness to provide a distinct look from the larger cards.
- **Voice Mic FAB:** Strictly circular (50% radius) to differentiate it as the primary specialized tool for the shopkeeper.

## Components

### Buttons
- **Primary:** Solid Medical Teal background with White text. Used for "Create Invoice" or "Save Sale."
- **Secondary (Hinglish):** Outlined buttons for "Add Udhaar" or "Check Khata."
- **Voice Mic FAB:** A 64px diameter Teal circle, positioned 24px from the bottom right. It features a persistent, soft pulse effect (2px expansion) to indicate it is "listening" or ready for input.

### Cards
- **Transaction Card:** White surface, 16px corners. Left side displays the customer name/Hinglish label; right side displays the currency amount in bold Plus Jakarta Sans.
- **Inventory Card:** Features a small status chip in the top right (e.g., "Low Stock" in Amber).

### Input Fields
- **Search:** Large 56px height search bar with a prominent "Search Medicine" placeholder.
- **Numeric Input:** Optimized for quantity entry with large + / - stepper buttons for easy thumb tapping.

### Bottom Navigation
- 5 items: **Home (Sale), Khata (Ledger), Inventory, Reports, Profile.**
- Active states use the Primary Teal color for the icon and label; inactive states use Neutral Grey.

### Chips
- Used for medicine categories or stock status. Rounded 100px (pill) with a subtle background tint of the status color (e.g., light green tint for "In Stock").