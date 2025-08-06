# 🔧 Amplitude Analytics Setup Instructions

## Issue: Analytics Events Not Appearing

If you're not seeing analytics events in Amplitude, the most likely cause is that you need to configure your **Amplitude API Key**.

## ⚠️ Current Status

Your project is configured with a placeholder API key. You need to replace it with your actual Amplitude project API key.

## 📋 Step-by-Step Setup

### 1. 🔑 Get Your Amplitude API Key

1. **Log in to Amplitude**: Go to [https://amplitude.com](https://amplitude.com)
2. **Select Your Project**: Choose your project or create a new one
3. **Navigate to Settings**: 
   - Click on **Settings** (gear icon) in the left sidebar
   - Go to **General** tab
4. **Copy API Key**: 
   - Find the **API Key** section
   - Copy your **API Key** (it looks like: `abc123def456ghi789jkl012mno345pq`)

### 2. 🛠️ Configure API Key in Your App

#### Option A: Direct Code Update (Quick)
1. Open `app/src/main/java/com/battleboat/AnalyticsManager.kt`
2. Find line 21: `private const val API_KEY = "YOUR_AMPLITUDE_API_KEY_HERE"`
3. Replace `"YOUR_AMPLITUDE_API_KEY_HERE"` with your actual API key:
   ```kotlin
   private const val API_KEY = "abc123def456ghi789jkl012mno345pq" // Your actual API key
   ```

#### Option B: Runtime Configuration (Recommended)
Pass the API key when initializing:
```kotlin
// In MainActivity.kt
analyticsManager.initialize("abc123def456ghi789jkl012mno345pq")
```

### 3. 🧪 Test Your Setup

1. **Build and run** your app:
   ```bash
   ./gradlew assembleDebug
   ```

2. **Check Android Studio Logcat** for analytics logs:
   - Filter by tag: `AnalyticsManager`
   - Look for messages like:
     ```
     🚀 Initializing Amplitude Analytics...
     ✅ Analytics initialized successfully!
     📈 Event tracked: Analytics Test
     ```

3. **Check Amplitude Dashboard**:
   - Go to **Events** in your Amplitude project
   - Look for events like "Analytics Test" and "Main Menu Opened"
   - Events may take a few minutes to appear

## 🔍 Debugging Checklist

### Check Logcat Output

Run your app and check for these log messages:

**✅ Success Messages:**
```
AnalyticsManager: 🚀 Initializing Amplitude Analytics...
AnalyticsManager: ✅ Analytics initialized successfully!
AnalyticsManager: 📈 Event tracked: Analytics Test
```

**❌ Error Messages:**
```
AnalyticsManager: ⚠️ No valid Amplitude API key provided! Analytics disabled.
AnalyticsManager: ❌ Analytics not initialized - skipping event
```

### Common Issues & Solutions

| Issue | Solution |
|-------|----------|
| `⚠️ No valid Amplitude API key provided!` | Configure your API key (see Step 2) |
| `❌ Analytics not initialized` | Call `analyticsManager.initialize()` before tracking events |
| `📊 Analytics disabled` | Check that analytics is enabled in app preferences |
| Events not appearing in Amplitude | Wait 5-10 minutes, check API key is correct |

### Verify Network Connectivity

Make sure your app has internet permission (already configured):
```xml
<!-- In AndroidManifest.xml -->
<uses-permission android:name="android.permission.INTERNET" />
```

## 🎯 Testing Analytics

The app includes a test function that verifies the analytics setup. When you run the app, check the logs for:

```
🧪 Testing Analytics Integration...
✅ Analytics Enabled: true
✅ Analytics Initialized: true
✅ Amplitude Instance: true
✅ Device ID: [device-id]
✅ User ID: Anonymous
📈 Event tracked: Analytics Test - {timestamp=..., test_type=integration_test}
```

## 📊 Expected Events

Once configured correctly, you should see these events in Amplitude:

1. **Analytics Test** - Fired on app start for testing
2. **Main Menu Opened** - When main menu loads
3. **Play Button Pressed** - When user starts game
4. **Tutorial Button Pressed** - When user opens tutorial
5. **Statistics Button Pressed** - When user views stats
6. **Settings Button Pressed** - When user opens settings
7. **App Foregrounded** - When app comes to foreground
8. **App Backgrounded** - When app goes to background

## 🔧 Advanced Configuration

### Set Custom User ID
```kotlin
// Set a custom user ID
analyticsManager.setUserId("user123")
```

### Enable/Disable Analytics
```kotlin
// Disable analytics
analyticsManager.setAnalyticsEnabled(false)

// Enable analytics
analyticsManager.setAnalyticsEnabled(true)
```

### Track Custom Events
```kotlin
// Track custom events with properties
analyticsManager.trackEvent("Custom Event", mapOf(
    "level" to 5,
    "score" to 1000,
    "difficulty" to "hard"
))
```

## 🆘 Still Having Issues?

1. **Check API Key**: Verify it's correct and from the right Amplitude project
2. **Check Internet**: Ensure device has internet connectivity
3. **Check Logs**: Look for error messages in Android Studio Logcat
4. **Wait**: Events can take 5-10 minutes to appear in Amplitude
5. **Test Environment**: Try in both debug and release builds

## 📚 Additional Resources

- [Amplitude Android SDK Documentation](https://amplitude.com/docs/guides-and-surveys/guides-and-surveys-android-sdk)
- [Amplitude Events Documentation](https://help.amplitude.com/hc/en-us/articles/229313067-Event-Tracking-Quick-Start-Guide)
- [Troubleshooting Amplitude](https://help.amplitude.com/hc/en-us/sections/200930908-Troubleshooting)

---

🎉 **Once configured, your BattleBoat app will start sending analytics events to Amplitude!** 