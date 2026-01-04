# Troubleshooting Guide

This guide helps you resolve common issues with WiFi Captive Auto-Login.

## Table of Contents

1. [Accessibility Service Issues](#accessibility-service-issues)
2. [Portal Not Detected](#portal-not-detected)
3. [Portal Detected But Not Clicking](#portal-detected-but-not-clicking)
4. [Profile Not Matching](#profile-not-matching)
5. [Network Connectivity Issues](#network-connectivity-issues)
6. [Storage and Profile Issues](#storage-and-profile-issues)
7. [Performance Issues](#performance-issues)
8. [Getting Help](#getting-help)

---

## Accessibility Service Issues

### Problem: "Accessibility Service Not Enabled" banner keeps appearing

**Symptoms:**
- Orange banner appears at top of main screen
- Portal automation doesn't work

**Solutions:**
1. **Enable the service:**
   - Tap "Open Settings" in the banner
   - Find "WiFi Captive Auto-Login" in the list
   - Toggle it ON
   - Return to the app

2. **If service doesn't appear in list:**
   - Restart the app
   - Check if app has necessary permissions
   - Uninstall and reinstall the app

3. **Service keeps turning off:**
   - Some device manufacturers have battery optimization that disables accessibility services
   - Go to Settings → Battery → Battery Optimization
   - Find "WiFi Captive Auto-Login" and set to "Not Optimized"

### Problem: Service enabled but not working

**Symptoms:**
- Service shows as enabled in settings
- Portal pages appear but nothing happens

**Solutions:**
1. **Restart the service:**
   - Disable the accessibility service
   - Re-enable it
   - Restart the app

2. **Check service permissions:**
   - Go to Settings → Apps → WiFi Captive Auto-Login → Permissions
   - Ensure all required permissions are granted

3. **Clear app data (last resort):**
   - Go to Settings → Apps → WiFi Captive Auto-Login → Storage
   - Tap "Clear Data"
   - Reconfigure your profiles

---

## Portal Not Detected

### Problem: App doesn't detect captive portal

**Symptoms:**
- Connected to Wi-Fi but no portal automation happens
- No notification appears

**Solutions:**
1. **Check profile configuration:**
   - Verify the SSID matches exactly (case-sensitive for EXACT match type)
   - Check if match type is correct (EXACT, CONTAINS, or REGEX)
   - Ensure profile is enabled (toggle switch is ON)

2. **Verify Wi-Fi connection:**
   - Check that you're actually connected to the Wi-Fi network
   - Try disconnecting and reconnecting
   - Check if portal page appears manually in browser

3. **Check trigger URL:**
   - Default trigger URLs: `http://captive.apple.com` or `http://www.msftconnecttest.com/redirect`
   - Some networks use different URLs - try opening in browser first
   - Update profile with correct trigger URL

4. **Service status:**
   - Ensure WifiMonitorService is running (check notification)
   - Restart the app if service stopped

### Problem: Portal detected but automation doesn't trigger

**Symptoms:**
- Notification appears saying portal detected
   - But no click happens

**Solutions:**
1. **Check click text configuration:**
   - Verify "Click Text Contains" matches button text on portal page
   - Button text is case-sensitive
   - Try adding more variations: "Accept", "Agree", "Connect", "Continue"

2. **Increase timeout:**
   - Portal page may load slowly
   - Increase timeout in profile settings (default: 10000ms)
   - Try 15000ms or 20000ms

3. **Check accessibility service:**
   - Ensure service is enabled and active
   - Try manually clicking the button to verify it's clickable

---

## Portal Detected But Not Clicking

### Problem: Button found but click doesn't work

**Symptoms:**
- Notification says "Portal detected"
- But button is not clicked automatically

**Solutions:**
1. **Verify button text:**
   - Check exact text on the button (including spaces, punctuation)
   - Update "Click Text Contains" in profile
   - Try "Exact Click Text" if button text is unique

2. **Check button visibility:**
   - Button must be visible on screen
   - Scroll down if button is below fold
   - Some portals require scrolling to see accept button

3. **Retry logic:**
   - App automatically retries 3 times
   - Wait a few seconds between attempts
   - If still fails, manually click once to verify button works

4. **Alternative click text:**
   - Try different variations: "I Agree", "Accept Terms", "Get Started"
   - Some portals use different languages
   - Check portal page source for exact button text

### Problem: Wrong button is clicked

**Symptoms:**
- App clicks a button but it's not the right one
   - Or clicks multiple buttons

**Solutions:**
1. **Use exact text:**
   - Switch to "Exact Click Text" instead of "Contains"
   - This ensures only the exact button is clicked

2. **Narrow down text:**
   - Use more specific text in "Click Text Contains"
   - Avoid generic words like "OK" or "Yes"

3. **Check page structure:**
   - Some portals have multiple buttons with similar text
   - Try to find unique text for the accept button

---

## Profile Not Matching

### Problem: Profile doesn't match Wi-Fi network

**Symptoms:**
- Connected to network but profile doesn't activate
- No automation happens

**Solutions:**
1. **Check SSID match:**
   - Verify actual SSID name (check Wi-Fi settings)
   - SSID may have hidden characters or spaces
   - Try using CONTAINS match type instead of EXACT

2. **Test regex pattern:**
   - If using REGEX, test pattern carefully
   - Common patterns:
     - `.*Airport.*` - matches any SSID containing "Airport"
     - `^Airport-.*` - matches SSIDs starting with "Airport-"
     - `.*WiFi.*|.*Guest.*` - matches SSIDs containing "WiFi" OR "Guest"

3. **Case sensitivity:**
   - EXACT match is case-sensitive
   - CONTAINS and REGEX are case-insensitive
   - Use CONTAINS if SSID case varies

4. **Use template:**
   - Try creating profile from template
   - Templates have pre-configured patterns for common networks

### Problem: Multiple profiles match same network

**Symptoms:**
- Multiple profiles could match
   - Wrong profile activates

**Solutions:**
1. **Prioritize profiles:**
   - More specific profiles should be listed first
   - App uses first matching profile
   - Reorder profiles in main screen

2. **Make profiles more specific:**
   - Use EXACT match for known SSIDs
   - Use more specific regex patterns
   - Avoid overly broad patterns like `.*`

---

## Network Connectivity Issues

### Problem: Connectivity validation fails

**Symptoms:**
- "Connectivity validation check failed" in logs
   - Or periodic re-checks fail

**Solutions:**
1. **Check trigger URL:**
   - Validation uses profile's trigger URL
   - Ensure URL is accessible
   - Some networks block certain URLs

2. **Disable validation:**
   - If validation causes issues, disable it in profile settings
   - Turn off "Enable Connectivity Validation"

3. **Adjust interval:**
   - Default is 5 minutes
   - Increase interval if checks are too frequent
   - Or decrease if you want more frequent checks

### Problem: Reconnection handling not working

**Symptoms:**
- Network disconnects and reconnects
   - But portal isn't re-triggered

**Solutions:**
1. **Enable reconnection handling:**
   - Check profile settings
   - Enable "Enable Reconnection Handling"
   - Reconnection window is 30 seconds

2. **Check network stability:**
   - If network disconnects frequently, reconnection may not trigger
   - Ensure stable Wi-Fi connection

---

## Storage and Profile Issues

### Problem: Profiles not saving

**Symptoms:**
- Changes to profiles don't persist
   - Profiles reset after app restart

**Solutions:**
1. **Check storage permissions:**
   - App needs storage permission
   - Go to Settings → Apps → WiFi Captive Auto-Login → Permissions
   - Grant storage permission

2. **Check available storage:**
   - Device may be out of storage space
   - Free up space and try again

3. **Clear and reconfigure:**
   - If profiles are corrupted, clear app data
   - Settings → Apps → WiFi Captive Auto-Login → Storage → Clear Data
   - Recreate profiles

### Problem: Profile import/export issues

**Symptoms:**
- Can't import profiles
   - Or exported profiles don't work

**Solutions:**
1. **Check file format:**
   - Profiles must be valid JSON
   - Verify file isn't corrupted
   - Check file encoding (should be UTF-8)

2. **Validate profile data:**
   - All required fields must be present
   - Check for missing or invalid values
   - Use profile editor to verify structure

---

## Performance Issues

### Problem: App uses too much battery

**Symptoms:**
- Battery drains faster than expected
   - App appears in battery usage list

**Solutions:**
1. **Disable unnecessary features:**
   - Turn off "Connectivity Validation" if not needed
   - Disable "Reconnection Handling" for stable networks
   - Reduce validation interval

2. **Optimize profiles:**
   - Disable profiles for networks you don't use
   - Remove unused profiles
   - Use more specific SSID patterns to reduce matching overhead

3. **Check background restrictions:**
   - Some devices restrict background activity
   - Go to Settings → Apps → WiFi Captive Auto-Login → Battery
   - Set to "Unrestricted" if available

### Problem: App is slow to respond

**Symptoms:**
- UI is sluggish
   - Profile loading takes time

**Solutions:**
1. **Clear cache:**
   - Go to Settings → Apps → WiFi Captive Auto-Login → Storage
   - Tap "Clear Cache" (not Clear Data)

2. **Reduce profile count:**
   - Too many profiles can slow down matching
   - Remove unused profiles
   - Combine similar profiles

3. **Restart app:**
   - Close and reopen the app
   - Or restart device if issues persist

---

## Getting Help

### Before Reporting Issues

1. **Check this guide** - Many issues are covered here
2. **Check app logs** - Enable developer options to view logs
3. **Test with simple profile** - Create a basic profile to isolate issues
4. **Verify network** - Ensure Wi-Fi and portal are working manually

### Information to Provide

When reporting issues, include:
- Device model and Android version
- App version
- Profile configuration (SSID, match type, trigger URL)
- Steps to reproduce
- Expected vs actual behavior
- Screenshots if applicable

### Common Solutions Summary

| Issue | Quick Fix |
|-------|-----------|
| Service not working | Re-enable accessibility service |
| Portal not detected | Check SSID match and trigger URL |
| Button not clicked | Verify click text matches button |
| Profile not matching | Use CONTAINS instead of EXACT |
| Battery drain | Disable connectivity validation |
| Profiles not saving | Check storage permissions |

---

## Advanced Troubleshooting

### Enable Debug Logging

1. Enable Developer Options on your device
2. Enable USB Debugging
3. Use `adb logcat` to view logs:
   ```bash
   adb logcat | grep -i wificaptive
   ```

### Check Service Status

1. Go to Settings → Accessibility
2. Find "WiFi Captive Auto-Login"
3. Tap to see service details
4. Check if service is running

### Reset App to Defaults

1. Go to Settings → Apps → WiFi Captive Auto-Login
2. Tap "Storage"
3. Tap "Clear Data" (this removes all profiles)
4. Restart app and reconfigure

### Test Profile Matching

1. Create a test profile with EXACT match
2. Use your current Wi-Fi SSID exactly
3. Enable the profile
4. Disconnect and reconnect to Wi-Fi
5. Check if profile activates

---

**Last Updated:** 2024-01-04

