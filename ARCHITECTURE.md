# Architecture Overview

## Modules
- core/profile: Profile models + matching logic
- core/storage: JSON persistence
- service/wifi: Wi-Fi detection + trigger
- service/accessibility: UI automation engine
- ui: Profile list + editor

## Data Flow
Wi-Fi connect
  → Profile match
  → Trigger captive portal
  → Accessibility scan + click
  → Disarm
