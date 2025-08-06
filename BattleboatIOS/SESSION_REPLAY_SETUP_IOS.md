# 📹 iOS Session Replay Setup Guide

## 🎯 Add Session Replay to BattleBoat iOS

This guide shows how to add [Amplitude Session Replay](https://amplitude.com/docs/session-replay) to your iOS BattleBoat project with **100% session capture**.

## 📦 Step 1: Add Session Replay Package Dependency

### **Using Xcode (Recommended)**

1. **Open your project** in Xcode
2. **Go to File → Add Package Dependencies...**
3. **Enter the URL**: `https://github.com/amplitude/amplitude-ios-session-replay`
4. **Select version**: `1.7.0` or later
5. **Click Add Package**
6. **Select target**: `BattleboatIOS`
7. **Click Add Package**

### **Manual Package.swift (Alternative)**

If using Swift Package Manager directly:

```swift
dependencies: [
    .package(url: "https://github.com/amplitude/Amplitude-Swift", from: "1.0.0"),
    .package(url: "https://github.com/amplitude/Amplitude-Engagement-Swift", from: "1.0.0"),
    .package(url: "https://github.com/amplitude/amplitude-ios-session-replay", from: "1.7.0")
]
```

## 🔧 Step 2: Enable Session Replay in Code

### **Update AnalyticsManager.swift**

1. **Uncomment the import** at the top:
```swift
import AmplitudeSessionReplay
```

2. **Uncomment the property**:
```swift
private var sessionReplayPlugin: AmplitudeSessionReplayPlugin? = nil
```

3. **Uncomment the initialization code** in `init()`:
```swift
sessionReplayPlugin = AmplitudeSessionReplayPlugin(
    sampleRate: 1.0, // Record 100% of sessions
    maskAllInputs: true, // Hide sensitive input data
    maskAllImages: false, // Show images (game elements)
    maskAllText: false // Show text (game UI)
)
amplitude.add(plugin: sessionReplayPlugin!)
```

4. **Uncomment the control methods**:
```swift
func startSessionReplay() {
    sessionReplayPlugin?.startRecording()
    print("📹 Session replay recording started")
}

func stopSessionReplay() {
    sessionReplayPlugin?.stopRecording()
    print("⏹️ Session replay recording stopped")
}

func isSessionReplayRecording() -> Bool {
    return sessionReplayPlugin?.isRecording() ?? false
}
```

### **Complete Updated AnalyticsManager.swift** (Key sections):

```swift
import Foundation
import UIKit
import AmplitudeSwift
import AmplitudeEngagementSwift
import AmplitudeSessionReplay  // ← Add this

class AnalyticsManager {
    static let shared = AnalyticsManager()
    
    private var amplitude: Amplitude
    private var sessionReplayPlugin: AmplitudeSessionReplayPlugin? = nil  // ← Add this
    
    private init() {
        let amplitudeEngagement = AmplitudeEngagement(amplitudeAPIKey)
        amplitude = Amplitude(configuration: Configuration(apiKey: amplitudeAPIKey))
        amplitude.add(plugin: amplitudeEngagement.getPlugin())
        
        // Initialize Session Replay with 100% sample rate
        sessionReplayPlugin = AmplitudeSessionReplayPlugin(
            sampleRate: 1.0, // Record 100% of sessions
            maskAllInputs: true, // Hide sensitive input data
            maskAllImages: false, // Show images (game elements)
            maskAllText: false // Show text (game UI)
        )
        amplitude.add(plugin: sessionReplayPlugin!)
        
        let bootOptions = AmplitudeBootOptions(
            user_id: amplitude.getUserId(),
            device_id: amplitude.getDeviceId(),
        )
        amplitudeEngagement.boot(options: bootOptions)
        
        print("📹 Session Replay: Recording 100% of sessions")
    }
    
    // ... existing methods ...
    
    // MARK: - Session Replay Management
    
    func startSessionReplay() {
        sessionReplayPlugin?.startRecording()
        print("📹 Session replay recording started")
    }
    
    func stopSessionReplay() {
        sessionReplayPlugin?.stopRecording()
        print("⏹️ Session replay recording stopped")
    }
    
    func isSessionReplayRecording() -> Bool {
        return sessionReplayPlugin?.isRecording() ?? false
    }
}
```

## 🔧 Step 3: Configuration Options

### **Sample Rate Settings**
```swift
// Record 100% of sessions (as requested)
sampleRate: 1.0

// Other options:
sampleRate: 0.5  // 50% of sessions
sampleRate: 0.1  // 10% of sessions
```

### **Privacy Configuration**
```swift
AmplitudeSessionReplayPlugin(
    sampleRate: 1.0,
    maskAllInputs: true,      // Hide password/text input fields
    maskAllImages: false,     // Show game images and icons
    maskAllText: false,       // Show game text and scores
    errorSampleRate: 1.0,     // Record 100% of error sessions
    sessionReplayRequiredPlugin: true  // Ensure it's always loaded
)
```

## 📱 Step 4: Test the Implementation

### **Build and Test**
1. **Build the project** - should compile without errors
2. **Run on device/simulator**
3. **Check Xcode console** for:
   ```
   📹 Session Replay: Recording 100% of sessions
   ```
4. **Play the game** for 1-2 minutes
5. **Check Amplitude dashboard** - replays should appear within 5 minutes

### **Debug Commands** (Optional)
Add test buttons in your UI:
```swift
// In a view controller
@IBAction func testSessionReplay(_ sender: Any) {
    AnalyticsManager.shared.startSessionReplay()
    print("Recording status: \(AnalyticsManager.shared.isSessionReplayRecording())")
}
```

## 📊 Step 5: View Replays in Amplitude

1. **Log into Amplitude dashboard**
2. **Navigate to Session Replay** (left sidebar)
3. **Filter by date** to see recent sessions
4. **Click Play** to watch user sessions
5. **Filter by events** like "Game Over" or "Start Game"

## 🎮 BattleBoat-Specific Benefits

### **Game Analysis**
- **Ship Placement Patterns**: See how users place ships
- **Tutorial Behavior**: Watch tutorial completion/abandonment
- **UI Interaction**: Identify confusing game elements
- **Bug Reproduction**: Replay sessions with reported issues

### **User Experience Insights**
- **Touch/Tap Patterns**: See exact user interactions
- **Navigation Flow**: How users move through game screens
- **Performance Issues**: Identify slow/laggy interactions
- **Feature Discovery**: What game features users find/miss

## ⚠️ Privacy & Compliance

### **Data Protection**
- **Sensitive masking** is enabled for input fields
- **Game content visible** for analysis (images, text, scores)
- **Retention period**: 30 days (expandable to 12 months)
- **User consent**: Consider adding opt-out in settings

### **iOS Privacy Requirements**
Session Replay doesn't require additional permissions, but consider adding privacy policy updates mentioning session recording.

## 🚀 Implementation Status

### ✅ **Code Ready**
- Import statements prepared
- Initialization code prepared  
- Control methods implemented
- Privacy settings configured

### 📋 **Next Steps**
1. **Add package dependency** via Xcode
2. **Uncomment prepared code** in AnalyticsManager.swift
3. **Test on device/simulator**
4. **Verify replays appear** in Amplitude dashboard

## 🔍 Troubleshooting

### **Common Issues**
- **Build errors**: Ensure Session Replay package is properly added
- **No replays**: Check network connectivity and API key
- **Missing events**: Verify analytics events are firing
- **Partial recordings**: Confirm sample rate is 1.0

### **Debug Tips**
- Check Xcode console for Session Replay messages
- Test on real device (simulator might have limitations)
- Verify network connectivity during recording
- Check Amplitude dashboard after 5-10 minutes

---

🎉 **Once complete, you'll have 100% session replay coverage on iOS to match your Android implementation!**

**Remember**: Session replays provide incredible insights into user behavior that traditional analytics can't capture. 