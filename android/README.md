# WiFi Captive Portal Auto-Login - Android App

## Setup Instructions

### Prerequisites
- Android Studio (latest version recommended)
- Android SDK with API level 24+ (Android 7.0+)
- JDK 8 or higher

### Building the App

1. Open the project in Android Studio
2. Sync Gradle files (File → Sync Project with Gradle Files)
3. Build the project (Build → Make Project)
4. Run on an emulator or physical device

### Building an APK

#### Debug APK (for testing)

**Method 1: Using Build Menu**
1. Go to **Build → Build Bundle(s) / APK(s) → Build APK(s)**
2. Wait for the build to complete
3. Click "locate" in the notification when it appears
4. The APK will be at: `app/build/outputs/apk/debug/app-debug.apk`

**Method 2: Using Gradle**
1. Open Gradle tool window (View → Tool Windows → Gradle)
2. Navigate to: `android` → `app` → `Tasks` → `build`
3. Double-click `assembleDebug`
4. Find the APK at: `app/build/outputs/apk/debug/app-debug.apk`

#### Release APK (for distribution/Play Store)

**First time setup (create signing key):**
1. Go to **Build → Generate Signed Bundle / APK**
2. Select **APK** → Click **Next**
3. Click **Create new...** to create a new keystore
4. Fill in the keystore information:
   - Key store path: Choose a secure location
   - Password: Create a strong password
   - Key alias: e.g., "wificaptive"
   - Key password: Create a strong password
   - Validity: 25+ years recommended
   - Certificate information: Fill in your details
5. Click **OK** and save the keystore file securely (you'll need it for updates)

**Build signed release APK:**
1. Go to **Build → Generate Signed Bundle / APK**
2. Select **APK** → Click **Next**
3. Select your keystore file and enter passwords
4. Select **release** build variant
5. Click **Finish**
6. The signed APK will be at: `app/build/outputs/apk/release/app-release.apk`

**Note:** Keep your keystore file and passwords secure! You'll need them for all future app updates.

### Required Permissions

The app requires the following permissions:
- **Internet**: To trigger captive portal detection
- **Access Network State**: To monitor Wi-Fi connections
- **Access Wi-Fi State**: To detect connected SSID
- **Change Wi-Fi State**: To interact with Wi-Fi
- **Access Fine/Coarse Location**: Required for Wi-Fi SSID access on Android 10+
- **Foreground Service**: To run the Wi-Fi monitoring service
- **Post Notifications**: For foreground service notification (Android 13+)

### Accessibility Service Setup

**IMPORTANT**: The app requires Accessibility Service to be enabled to automatically click portal buttons.

1. After installing the app, go to **Settings → Accessibility**
2. Find "WiFi Captive Auto-Login" in the list
3. Enable the service
4. Grant all requested permissions

### Usage

1. **Add Profiles**: Tap the + button to add a new portal profile
   - Enter the SSID (Wi-Fi network name)
   - Select match type (Exact, Contains, or Regex)
   - Set trigger URL (usually `http://captive.apple.com` or `http://www.msftconnecttest.com/redirect`)
   - Configure click text matching (exact or contains)
   - Set timeout and cooldown values

2. **Enable/Disable**: Toggle profiles on/off using the switch in the profile list

3. **Edit/Delete**: Tap a profile to edit or delete it

4. **Automatic Operation**: Once enabled, the app will:
   - Monitor Wi-Fi connections
   - Detect when a matching SSID is connected
   - Trigger the captive portal
   - Automatically click the accept/connect button

### Default Profiles

The app comes with two default profiles:
- **Airport Wi-Fi**: Matches SSIDs containing "Airport" (regex)
- **Starbucks**: Matches SSIDs containing "Starbucks" (regex)

### Architecture

- **core/profile**: Profile data models and SSID matching logic
- **core/storage**: JSON-based profile persistence
- **service/wifi**: Wi-Fi connection monitoring and portal triggering
- **service/accessibility**: UI automation engine for clicking portal buttons
- **ui**: Profile management interface (list, add, edit)

### Notes

- The app uses a foreground service to continuously monitor Wi-Fi connections
- Only one click is performed per portal session (enforced by cooldown)
- Profiles are stored locally in JSON format
- The accessibility service only interacts with clickable elements matching the configured text
