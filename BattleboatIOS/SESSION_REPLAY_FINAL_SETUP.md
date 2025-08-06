# 🍎 iOS Session Replay - Final Setup Steps

## ✅ **Current Status**

- ✅ **Android**: Fully active and recording 100% of sessions
- 🔄 **iOS**: Code prepared, package installation needed

## 📦 **Step 1: Add Session Replay Package in Xcode**

### **Using Xcode Package Manager**

1. **Open BattleboatIOS.xcodeproj** in Xcode
2. **Go to File** → **Add Package Dependencies...**
3. **Enter Package URL**: 
   ```
   https://github.com/amplitude/amplitude-ios-session-replay
   ```
4. **Select version**: `1.7.0` or **"Up to Next Major Version"**
5. **Click "Add Package"**
6. **Select target**: Make sure `BattleboatIOS` is checked
7. **Click "Add Package"** again

## 🔧 **Step 2: Activate the Session Replay Code**

Once the package is added, make these changes in `AnalyticsManager.swift`:

### **1. Uncomment the import**
```swift
// Change this:
// import AmplitudeSessionReplay  // TODO: Uncomment after adding package via Xcode

// To this:
import AmplitudeSessionReplay
```

### **2. Uncomment the property**
```swift
// Change this:
// private var sessionReplayPlugin: AmplitudeSessionReplayPlugin? = nil  // TODO: Uncomment after adding package

// To this:
private var sessionReplayPlugin: AmplitudeSessionReplayPlugin? = nil
```

### **3. Uncomment the initialization code**
```swift
// Change this:
// TODO: Uncomment after adding AmplitudeSessionReplay package via Xcode
/*
sessionReplayPlugin = AmplitudeSessionReplayPlugin(
    sampleRate: 1.0, // Record 100% of sessions
    maskAllInputs: true, // Hide sensitive input data
    maskAllImages: false, // Show images (game elements)
    maskAllText: false // Show text (game UI)
)
amplitude.add(plugin: sessionReplayPlugin!)
*/

// To this:
sessionReplayPlugin = AmplitudeSessionReplayPlugin(
    sampleRate: 1.0, // Record 100% of sessions
    maskAllInputs: true, // Hide sensitive input data
    maskAllImages: false, // Show images (game elements)
    maskAllText: false // Show text (game UI)
)
amplitude.add(plugin: sessionReplayPlugin!)
```

### **4. Uncomment the status method**
```swift
// Change this:
func getSessionReplayStatus() -> String {
    // TODO: Uncomment after adding AmplitudeSessionReplay package
    /*
    if sessionReplayPlugin != nil {
        return "✅ Session Replay: Active (100% capture rate)"
    } else {
        return "❌ Session Replay: Not initialized"
    }
    */
    return "📦 Session Replay: Add package first, then uncomment code"
}

// To this:
func getSessionReplayStatus() -> String {
    if sessionReplayPlugin != nil {
        return "✅ Session Replay: Active (100% capture rate)"
    } else {
        return "❌ Session Replay: Not initialized"
    }
}
```

### **5. Update the status message**
```swift
// Change this:
print("📹 Session Replay: Ready to enable (add package first)")

// To this:
print("📹 Session Replay: Recording 100% of sessions")
```

## 🧪 **Step 3: Test the Implementation**

### **Build and Run**
1. **Build the project** - should compile without errors
2. **Run on device/simulator**
3. **Check Xcode console** for:
   ```
   📹 Session Replay: Recording 100% of sessions
   ```

### **Verify in Amplitude Dashboard**
1. **Play the game** for 1-2 minutes
2. **Check Amplitude dashboard** → **Session Replay** (left sidebar)
3. **Sessions should appear** within 5-10 minutes
4. **Filter by platform** to see iOS vs Android sessions

## ✅ **Expected Final Status**

### **After Package Installation & Code Activation**

**iOS Console Output:**
```
📹 Session Replay: Recording 100% of sessions
```

**Amplitude Dashboard:**
- iOS sessions appearing with 100% capture rate
- Visual recordings of user gameplay
- Event timeline linked to replay moments
- Cross-platform comparison with Android data

## 🎮 **What You'll See in Session Replays**

### **BattleBoat iOS Specific**
- **Ship placement**: Touch interactions with grid
- **Targeting behavior**: Tap patterns on enemy grid  
- **Tutorial navigation**: Step-by-step user flow
- **Menu interactions**: Button taps and navigation
- **Game flow**: Complete session from start to game over

### **Cross-Platform Insights**
- **iOS vs Android**: Compare user behavior patterns
- **Touch vs Click**: Different interaction styles
- **UI effectiveness**: Platform-specific usability
- **Performance**: Smooth vs laggy interactions

## 🚀 **Final Result**

Once complete, you'll have:

✅ **Android**: 100% session capture active  
✅ **iOS**: 100% session capture active  
✅ **Cross-platform**: Complete behavioral analytics  
✅ **Game insights**: Visual user journey data  
✅ **Privacy compliant**: Sensitive data masked  

## 📋 **Summary Checklist**

- [ ] **Add Session Replay package** in Xcode
- [ ] **Uncomment import** statement  
- [ ] **Uncomment property** declaration
- [ ] **Uncomment initialization** code
- [ ] **Uncomment status method**
- [ ] **Update print message**
- [ ] **Build and test** 
- [ ] **Verify in Amplitude dashboard**

**🎉 Once these steps are complete, both iOS and Android will have 100% Session Replay coverage!** 