# 📹 Amplitude Session Replay Implementation Guide

## 🎯 Overview

This guide implements [Amplitude Session Replay](https://amplitude.com/docs/session-replay) for both **Android** and **iOS** BattleBoat projects with **100% session capture rate** as requested.

## ✨ Session Replay Benefits

- **Visual User Journey**: See exactly how users interact with your app
- **Bug Troubleshooting**: Watch sessions to identify issues faster  
- **UX Optimization**: Identify drop-off points and improve conversions
- **Qualitative Insights**: Get context beyond just event data

## 🤖 Android Implementation

### 1. **Add Dependency**

```gradle
// In app/build.gradle
dependencies {
    // Existing Amplitude dependencies
    implementation 'com.amplitude:analytics-android:1.22.0'
    implementation 'com.amplitude:amplitude-engagement-android:1.0+'
    
    // Session Replay Plugin
    implementation 'com.amplitude:plugin-session-replay-android:0.+'
}
```

### 2. **Initialize Session Replay**

```kotlin
// In AnalyticsManager.kt
import com.amplitude.android.sessionreplay.SessionReplayPlugin

class AnalyticsManager {
    private var sessionReplayPlugin: SessionReplayPlugin? = null
    
    fun initialize() {
        try {
            // Initialize Session Replay with 100% capture rate
            sessionReplayPlugin = SessionReplayPlugin(
                sampleRate = 1.0, // Capture 100% of sessions
                maskAllInputs = true, // Hide sensitive input data
                maskAllImages = false, // Show images (game elements)
                maskAllText = false // Show text (game UI)
            )
            
            amplitude = Amplitude(Configuration(
                apiKey = API_KEY,
                context = context,
                autocapture = setOf(
                    AutocaptureOption.SESSIONS,
                    AutocaptureOption.APP_LIFECYCLES,
                    AutocaptureOption.SCREEN_VIEWS
                )
            ))
            
            // Add Session Replay plugin
            amplitude!!.add(sessionReplayPlugin!!)
            
            Log.d(TAG, "📹 Session Replay: Recording 100% of sessions")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to initialize Session Replay", e)
        }
    }
    
    // Manual controls (optional)
    fun startSessionReplay() {
        sessionReplayPlugin?.startRecording()
        Log.d(TAG, "📹 Session replay recording started")
    }
    
    fun stopSessionReplay() {
        sessionReplayPlugin?.stopRecording()
        Log.d(TAG, "⏹️ Session replay recording stopped")
    }
}
```

### 3. **Privacy Configuration**

Add to your `AndroidManifest.xml` if needed:

```xml
<!-- For network access -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

## 🍎 iOS Implementation

### 1. **Add Session Replay SDK**

```swift
// In iOS project Package.swift or CocoaPods
.package(
    url: "https://github.com/amplitude/Amplitude-iOS", 
    from: "8.19.0"
),
.package(
    url: "https://github.com/amplitude/amplitude-ios-session-replay", 
    from: "1.7.0"
)
```

Or via CocoaPods:
```ruby
# In Podfile
pod 'Amplitude', '~> 8.19.0'
pod 'AmplitudeSessionReplay', '~> 1.7.0'
```

### 2. **Initialize Session Replay**

```swift
// In your iOS AnalyticsManager or AppDelegate
import Amplitude
import AmplitudeSessionReplay

class AnalyticsManager {
    func initialize() {
        // Initialize Amplitude
        let amplitude = Amplitude(configuration: Configuration(
            apiKey: "909b2239fab57efd4268eb75dbc28d30"
        ))
        
        // Configure Session Replay with 100% capture rate
        let sessionReplayPlugin = AmplitudeSessionReplayPlugin(
            sampleRate: 1.0, // Capture 100% of sessions
            maskAllInputs: true, // Hide sensitive inputs
            maskAllImages: false, // Show images (game elements)
            maskAllText: false // Show text (game UI)
        )
        
        // Add Session Replay plugin
        amplitude.add(plugin: sessionReplayPlugin)
        
        print("📹 Session Replay: Recording 100% of sessions")
    }
}
```

### 3. **Privacy and Permissions**

Add to your `Info.plist` if needed:
```xml
<key>NSCameraUsageDescription</key>
<string>Used for session replay functionality</string>
```

## 🔧 Configuration Options

### **Sample Rate Settings**
- `sampleRate: 1.0` = 100% of sessions (as requested)
- `sampleRate: 0.5` = 50% of sessions  
- `sampleRate: 0.1` = 10% of sessions

### **Privacy Settings**
```kotlin
// Android
SessionReplayPlugin(
    sampleRate = 1.0,
    maskAllInputs = true,      // Hide password/credit card fields
    maskAllImages = false,     // Show game images/icons
    maskAllText = false,       // Show game text/scores
    errorSampleRate = 1.0      // Capture 100% of error sessions
)
```

```swift
// iOS  
AmplitudeSessionReplayPlugin(
    sampleRate: 1.0,
    maskAllInputs: true,       // Hide password/credit card fields
    maskAllImages: false,      // Show game images/icons
    maskAllText: false,        // Show game text/scores
    errorSampleRate: 1.0       // Capture 100% of error sessions
)
```

## 📊 Viewing Session Replays

### **In Amplitude Dashboard**

1. **Navigate to Session Replay** in the left sidebar
2. **Filter by date range** to find recent sessions
3. **Filter by events** (e.g., "Game Over", "Tutorial Completed")
4. **Click Play** to watch user sessions
5. **Add to dashboards** for team sharing

### **Integration with Analytics**

Session Replays automatically link to:
- ✅ **Event streams** - Click events to jump to replay moments
- ✅ **Funnels** - See where users drop off visually
- ✅ **User Lookup** - Watch specific user sessions
- ✅ **Cohorts** - Compare behavior across user groups

## 🎮 BattleBoat-Specific Benefits

### **Game Flow Analysis**
- Watch how users interact with ship placement
- See tutorial completion behavior
- Identify UI/UX pain points

### **Bug Identification**
- Replay sessions where users reported issues
- See exact tap/click patterns causing problems
- Verify fix effectiveness

### **Feature Usage**
- Watch how users discover game features
- See interaction patterns with settings/stats
- Identify unused UI elements

## ⚠️ Privacy Considerations

### **Data Protection**
- Session Replay respects [Amplitude's privacy settings](https://amplitude.com/docs/session-replay)
- **Sensitive data masking** is enabled by default
- **User consent** should be obtained where required by law
- **Retention**: Replays are stored for 30 days (expandable to 12 months)

### **GDPR/Privacy Compliance**
```kotlin
// Allow users to opt out
fun setSessionReplayEnabled(enabled: Boolean) {
    if (enabled) {
        sessionReplayPlugin?.startRecording()
    } else {
        sessionReplayPlugin?.stopRecording()
    }
}
```

## 🚀 Implementation Status

### ✅ **Ready for Android**
- Dependency identified: `com.amplitude:plugin-session-replay-android:0.+`
- Code structure prepared in `AnalyticsManager.kt`
- Privacy settings configured

### ✅ **Ready for iOS**
- SDK identified: `AmplitudeSessionReplay`
- Implementation pattern documented
- Integration points defined

## 📋 Next Steps

### **For Android**
1. **Verify dependency**: Ensure the Session Replay plugin imports correctly
2. **Test recording**: Use debug interface to verify sessions are captured
3. **Check Amplitude dashboard**: Confirm replays appear within 5 minutes

### **For iOS**
1. **Add iOS Session Replay SDK** to your iOS project
2. **Implement initialization code** in iOS AnalyticsManager
3. **Test across both platforms** to ensure consistency

## 🔍 Troubleshooting

### **Common Issues**
- **No replays appearing**: Check network connectivity and API key
- **Partial recordings**: Verify sample rate is set to 1.0
- **Missing events**: Ensure analytics events are firing correctly
- **Privacy concerns**: Review masking settings for your use case

### **Debug Commands**
```kotlin
// Android - Test Session Replay
analyticsManager.testAnalytics() // Will show Session Replay status
analyticsManager.startSessionReplay() // Manual start
```

---

🎉 **With 100% session capture, you'll have complete visibility into user behavior across both Android and iOS BattleBoat apps!**

**Next**: Implement the iOS version and verify both platforms are capturing sessions in your Amplitude dashboard. 