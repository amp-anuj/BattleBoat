# 📱 Manual iOS Session Replay Setup

## 🚨 **Issue: Module Not Found**

The automated package addition didn't work correctly. Let's add the Session Replay package manually using Xcode.

## 📦 **Step 1: Manual Package Installation**

### **In Xcode:**

1. **Open `BattleboatIOS.xcodeproj`** in Xcode

2. **File** → **Add Package Dependencies...**

3. **Enter the official repository URL**:
   ```
   https://github.com/amplitude/AmplitudeSessionReplay-iOS
   ```

4. **Dependency Rule**: Select **"Up to Next Major Version"** or **"Branch: main"**

5. **Click "Add Package"**

6. **Select Products**: 
   - ✅ **Check: `AmplitudeSwiftSessionReplayPlugin`** 
   - ✅ **Target: `BattleboatIOS`**

7. **Click "Add Package"**

## 🔧 **Step 2: Verify Package Installation**

After adding the package, check that:
- **Package appears** in Xcode Project Navigator under "Package Dependencies"
- **No build errors** when building the project
- **Module can be imported** (test with a simple import)

## 📝 **Step 3: Activate the Code**

Once the package is successfully added, make these changes in `AnalyticsManager.swift`:

### **Change 1: Uncomment Import**
```swift
// From:
// import AmplitudeSwiftSessionReplayPlugin  // TODO: Add package manually via Xcode

// To:
import AmplitudeSwiftSessionReplayPlugin
```

### **Change 2: Uncomment Property**
```swift
// From:
// private var sessionReplayPlugin: AmplitudeSwiftSessionReplayPlugin? = nil  // TODO: Uncomment after adding package

// To:
private var sessionReplayPlugin: AmplitudeSwiftSessionReplayPlugin? = nil
```

### **Change 3: Uncomment Initialization**
```swift
// From:
// TODO: Uncomment after adding package manually
/*
sessionReplayPlugin = AmplitudeSwiftSessionReplayPlugin(sampleRate: 1.0)
amplitude.add(plugin: sessionReplayPlugin!)
*/

// To:
sessionReplayPlugin = AmplitudeSwiftSessionReplayPlugin(sampleRate: 1.0)
amplitude.add(plugin: sessionReplayPlugin!)
```

### **Change 4: Update Status Messages**
```swift
// From:
print("📹 Session Replay: Package needs manual installation")

// To:
print("📹 Session Replay: Recording 100% of sessions")
```

```swift
// From:
func getSessionReplayStatus() -> String {
    return "📦 Session Replay: Manual package installation required"
}

// To:
func getSessionReplayStatus() -> String {
    if sessionReplayPlugin != nil {
        return "✅ Session Replay: Active (100% capture rate)"
    } else {
        return "❌ Session Replay: Not initialized"
    }
}
```

## 🧪 **Step 4: Test Installation**

1. **Build the project** after adding package
2. **Check for "No such module" errors** - should be resolved
3. **Run on device/simulator**
4. **Check console** for: `📹 Session Replay: Recording 100% of sessions`

## 🔍 **Troubleshooting**

### **If Package Installation Fails:**

**Option A: Try Different Repository URLs**
- Main repo: `https://github.com/amplitude/AmplitudeSessionReplay-iOS`  
- Branch specific: `https://github.com/amplitude/AmplitudeSessionReplay-iOS.git`

**Option B: Check Xcode Version**
- Ensure you're using **Xcode 14+**
- Session Replay requires **iOS 13+** minimum

**Option C: Clean and Rebuild**
```bash
# In Xcode:
Product → Clean Build Folder
Product → Build
```

### **If Module Still Not Found:**

1. **Check Package Dependencies** in Xcode Project Navigator
2. **Verify target membership** for the package
3. **Try removing and re-adding** the package
4. **Restart Xcode** after adding package

## 📚 **Reference Documentation**

Based on: [Amplitude Session Replay iOS Plugin](https://amplitude.com/docs/session-replay/session-replay-ios-plugin)

### **Correct Package Details:**
- **Repository**: `https://github.com/amplitude/AmplitudeSessionReplay-iOS`
- **Product**: `AmplitudeSwiftSessionReplayPlugin`
- **Minimum iOS**: 13.0
- **Swift SDK**: Requires Amplitude-Swift 1.9.0+

## 🎯 **Current Status**

### **✅ Android** - Session Replay active (100% capture)
### **🔄 iOS** - Waiting for manual package installation

## 📞 **Alternative: CocoaPods**

If Swift Package Manager continues to have issues, you can try CocoaPods:

```ruby
# Add to Podfile:
pod 'AmplitudeSessionReplay', :git => 'https://github.com/amplitude/AmplitudeSessionReplay-iOS.git'
pod 'AmplitudeSwiftSessionReplayPlugin', :git => 'https://github.com/amplitude/AmplitudeSessionReplay-iOS.git'
```

Then run:
```bash
pod install
```

---

**Once the package is successfully added manually, your iOS app will have 100% Session Replay coverage to match Android!** 🚀📱 