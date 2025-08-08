# Complete Analytics Events List - BattleBoat Cross-Platform

## 📊 **All Events and Properties by Platform**

### **🎮 Core Game Events**

| Event Name | Properties | Web | iOS | Android | Description |
|------------|------------|-----|-----|---------|-------------|
| **Game Initialized** | None | ✅ | ❌ | ❌ | Game setup begins (Web only) |
| **Game Started** | Platform, Version, Timestamp | ✅ | ✅ | ✅ | Gameplay begins |
| **Game Ended** | Win, "Shots Taken" | ✅ | ✅ | ✅ | Game finishes |
| **Ship Selected** | Ship, "Ship Type" | ✅ | ✅ | ✅ | Player selects a ship to place |
| **Ship Placed** | Ship, Success, X, Y | ✅ | ✅ | ✅ | Ship placement attempt |
| **Ship Rotated** | Ship | ✅ | ✅ | ✅ | Ship orientation changed |
| **Shot Fired** | X, Y, Hit, "Consecutive Hits", Player* | ✅ | ✅ | ✅ | Attack fired at grid |

*Player property: "human" or "computer" (iOS/Android only for computer shots)

---

### **🎯 Platform-Specific Events**

#### **📱 iOS-Only Events**

| Event Name | Properties | Description |
|------------|------------|-------------|
| **Tutorial Step** | Step, "Step Name" | Individual tutorial step tracked |
| **Tutorial Skipped** | None | Player skips tutorial |
| **Tutorial Completed** | None | Player completes tutorial |
| **Ships Placed Randomly** | None | Random ship placement used |
| **Probability Heatmap Shown** | None | AI probability heatmap displayed |
| **Probability Heatmap Hidden** | None | AI probability heatmap hidden |
| **Statistics Reset** | None | Player resets game statistics |
| **Human Grid Analysis** | Complex grid data | Ship placement pattern analysis |

#### **🤖 Android-Only Events**

| Event Name | Properties | Description |
|------------|------------|-------------|
| **Main Menu Opened** | None | App main menu displayed |
| **Play Button Pressed** | None | User starts game from menu |
| **Tutorial Button Pressed** | None | User selects tutorial option |
| **Statistics Button Pressed** | None | User selects statistics view |
| **Settings Button Pressed** | None | User accesses settings |
| **Ship Sunk** | "Ship Type" | Specific ship destroyed |
| **Tutorial Step** | Step | Individual tutorial step |
| **Tutorial Completed** | None | Tutorial finished |
| **Tutorial Skipped** | Step | Tutorial abandoned at step |
| **Game Quit** | None | Player exits game |

---

## 🔍 **Detailed Property Specifications**

### **Common Properties**

| Property Name | Type | Values | Platforms | Description |
|---------------|------|--------|-----------|-------------|
| **Ship** | String | "carrier", "battleship", "destroyer", "submarine", "patrolboat" | All | Ship type identifier |
| **Success** | Boolean | true, false | All | Whether action succeeded |
| **X** | Integer | 0-9 | All | Grid column coordinate |
| **Y** | Integer | 0-9 | All | Grid row coordinate |
| **Hit** | Boolean | true, false | All | Whether shot hit a ship |
| **Win** | Boolean | true, false | All | Whether player won game |

### **Complex Properties**

| Property Name | Type | Values | Platforms | Description |
|---------------|------|--------|-----------|-------------|
| **"Shots Taken"** | Integer | 1-100+ | All | Total shots fired in game |
| **"Consecutive Hits"** | Integer | 0-17 | Web, iOS, Android | Sequential hits without miss |
| **"Ship Type"** | String | Same as Ship | Web | Duplicate ship identifier |
| **Platform** | String | "iOS" | iOS | Platform identifier |
| **Version** | String | "1.0" | iOS | App version |
| **Timestamp** | Number | Unix timestamp | iOS | Event timestamp |
| **Player** | String | "human", "computer" | iOS, Android | Who fired the shot |

### **iOS-Specific Properties**

| Property Name | Type | Values | Description |
|---------------|------|--------|-------------|
| **Step** | String | Tutorial step identifier | Current tutorial step |
| **"Step Name"** | String | Human-readable step name | Tutorial step description |
| **"User ID"** | String | User identifier | Player ID for analysis |
| **"Game Performance"** | String | Performance level | Player skill assessment |
| **"[Ship] X"** | Integer | Grid coordinate | Ship X position in analysis |
| **"[Ship] Y"** | Integer | Grid coordinate | Ship Y position in analysis |
| **"[Ship] Direction"** | String | "horizontal", "vertical" | Ship orientation in analysis |

### **Android-Specific Properties**

| Property Name | Type | Values | Description |
|---------------|------|--------|-------------|
| **"Ship Type"** | String | Ship type name | Type of ship that was sunk |
| **Step** | Integer | Step number | Tutorial step number |

---

## 📊 **Event Count Summary**

| Platform | Total Events | Shared Events | Platform-Specific |
|----------|-------------|---------------|-------------------|
| **Web** | 7 | 7 | 0 |
| **iOS** | 15 | 7 | 8 |
| **Android** | 16 | 7 | 9 |

---

## 🎯 **Usage Examples**

### **Web (JavaScript)**
```javascript
// Core game events
amplitude.track('Game Started');
amplitude.track('Ship Placed', {
    Ship: 'carrier',
    Success: true,
    X: 1,
    Y: 1
});
amplitude.track('Shot Fired', {
    X: 5,
    Y: 3,
    Hit: true,
    "Consecutive Hits": 2
});
```

### **iOS (Swift)**
```swift
// Core game events
AnalyticsManager.shared.trackStartGame()
AnalyticsManager.shared.trackPlaceShip(shipType: .carrier, success: true, x: 1, y: 1)
AnalyticsManager.shared.trackPlayerShoot(x: 5, y: 3, hit: true, consecutiveHits: 2)

// iOS-specific events
AnalyticsManager.shared.trackTutorialStep(step: .selectShip)
AnalyticsManager.shared.trackShowProbabilityHeatmap()
```

### **Android (Kotlin)**
```kotlin
// Core game events
analyticsManager.trackStartGame()
analyticsManager.trackPlaceShip("carrier", true, 1, 1)
analyticsManager.trackPlayerShoot(5, 3, true, 2)

// Android-specific events
analyticsManager.trackEvent("Main Menu Opened")
analyticsManager.trackEvent("Ship Sunk", mapOf("Ship Type" to "destroyer"))
```

---

## 📋 **Notes**

1. **Shared Events**: All platforms implement the 7 core game events with identical naming
2. **Property Consistency**: Same properties are used across platforms for shared events
3. **Platform Extensions**: Each platform adds events specific to its user experience
4. **Naming Standards**: All events follow "[Noun] [Past Tense Verb]" format in Title Case
5. **Multi-word Properties**: Use quotes in JavaScript, natural naming in Swift/Kotlin

---

**Last Updated**: August 2025  
**Total Events Tracked**: 23+ unique events across all platforms 