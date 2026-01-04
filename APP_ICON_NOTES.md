# App Icon Design Notes

## Current Status
The app currently uses a placeholder icon (`@android:drawable/ic_dialog_info`). A custom app icon needs to be designed and added.

## Requirements

### Technical Specifications
- **Size:** 512 x 512 pixels (required for Play Store)
- **Format:** PNG with alpha channel (32-bit)
- **Safe Zone:** Keep important content within 384 x 384 pixels (75% of icon)
- **Background:** Can be transparent or solid color
- **File Location:** `android/app/src/main/res/mipmap-*/ic_launcher.png`

### Design Requirements
- Must be recognizable at small sizes (48dp on device)
- Should represent Wi-Fi connectivity and automation
- Modern, clean design matching Material Design guidelines
- Consider app's color scheme (currently Material Blue)

## Design Suggestions

### Concept Ideas
1. **Wi-Fi Signal + Checkmark** - Wi-Fi signal waves with a checkmark overlay
2. **Wi-Fi + Automation Symbol** - Wi-Fi icon with gear or automation symbol
3. **Network + Portal** - Network nodes with portal/gateway symbol
4. **Wi-Fi + Auto** - Wi-Fi signal with "AUTO" text or symbol

### Color Scheme
- Primary: Material Blue (#2196F3) or similar
- Accent: Green for success/automation (#4CAF50)
- Background: White or light gradient

### Style
- Flat design (Material Design)
- Simple, clean lines
- High contrast for visibility
- No text (icon only)

## Implementation Steps

1. **Design Icon** - Create 512x512px design
2. **Generate Sizes** - Create all required sizes:
   - `mipmap-mdpi`: 48x48px
   - `mipmap-hdpi`: 72x72px
   - `mipmap-xhdpi`: 96x96px
   - `mipmap-xxhdpi`: 144x144px
   - `mipmap-xxxhdpi`: 192x192px
3. **Update Manifest** - Update `AndroidManifest.xml` to reference new icon
4. **Test** - Verify icon displays correctly on device

## Tools
- **Design:** Figma, Adobe Illustrator, Sketch, or similar
- **Export:** Android Asset Studio (https://romannurik.github.io/AndroidAssetStudio/icons-launcher.html)
- **Validation:** Test on multiple device sizes and densities

## Current Placeholder
The app currently uses `@android:drawable/ic_dialog_info` as a temporary icon. This should be replaced before Play Store submission.

---

**Status:** Pending - Custom icon design required

