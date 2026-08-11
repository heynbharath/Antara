# Calm UI Design System Specification

Antara's user interface is built on **Calm Design** principles. The product avoids flashy, attention-seeking elements in favor of a silent, responsive interface. Because energy efficiency directly impacts device survivability in disconnected mesh environments, our design system is explicitly optimized for OLED battery preservation.

---

## 1. Core Color System (Battery Optimized)

Antara uses a **True Black** foundation. By using absolute black (`#000000`), OLED displays turn off individual subpixels, saving up to 40% screen battery consumption compared to standard gray or white layouts.

```
Background:  #000000 (Pure black, off-pixels)
Surface:     #0A0A0C (Deep carbon, subtle container contrast)
Border:      #1C1C1E (Thin slate divider)
Text Primary:#FFFFFF (Pure white, high contrast)
Text Secondary:#8E8E93 (Muted silver, low prominence)
Accent:      #D4AF37 (Soft raw gold, reserved for indicators)
```

---

## 2. Typography Hierarchy

We use Google Fonts **Outfit** for headers (display, titles) to project an elegant, geometric form, and **Inter** for message bodies to ensure high legibility under poor lighting conditions.

| Name | Font Family | Size | Weight | Line Height | Tracking | Use Case |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Display** | Outfit | 36px | 600 (SemiBold) | 44px | -0.02em | Hero vision headers |
| **Title Medium** | Outfit | 20px | 500 (Medium) | 28px | -0.01em | Thread names |
| **Body Large** | Inter | 16px | 400 (Regular) | 24px | 0.0em | Chat message content |
| **Body Small** | Inter | 12px | 400 (Regular) | 16px | 0.01em | Time stamps, status indicators |

---

## 3. Glassmorphic Surface Styling (Background Blur)

Where overlays are necessary (e.g., drawer navigations or search results), we implement a minimal, premium glassmorphism effect using CSS backdrop-filters:

```css
.glass-overlay {
  background: rgba(10, 10, 12, 0.7); /* 70% opacity carbon surface */
  backdrop-filter: blur(20px) saturate(180%);
  border: 1px solid rgba(28, 28, 30, 0.5);
  border-radius: 12px;
}
```

---

## 4. Spacing System (Logical Grid)

We enforce a strict 8-pixel grid scale for all margins, padding, and layout bounds:

```
Space 1:   4px   (Micro-adjustments, borders)
Space 2:   8px   (Inside container padding)
Space 3:   16px  (Standard component spacing)
Space 4:   24px  (Layout margins)
Space 5:   32px  (Section headers)
```

---

## 5. Motion, Curves & Micro-Animations

Transitions must feel organic and fluid, avoiding bouncy or dramatic animations. We use custom easing profiles that match physical momentum.

### Easing Tokens:
*   **Standard Easing:** `cubic-bezier(0.2, 0.8, 0.2, 1)` (Fast acceleration, long decay).
*   **Entrance Easing:** `cubic-bezier(0.0, 0.0, 0.2, 1)` (Decelerating slide-in).
*   **Exit Easing:** `cubic-bezier(0.4, 0.0, 1, 1)` (Accelerating exit).

### Micro-Animation Triggers:
1.  **Message Insertion:** Messages slide up slightly (4px) and fade in over 200ms using the *Entrance Easing* curve.
2.  **Peer Discovery Pulse:** When a new peer is matched during active scanning, their presence indicator performs a single, soft fade transition over 600ms, avoiding flashing elements.
3.  **Active Sync State:** A subtle, slow fade (opacity pulsing from 0.4 to 1.0) indicates dynamic background data synchronization, only visible on tap.
