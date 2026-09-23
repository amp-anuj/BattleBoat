# Analytics Naming Standards - BattleBoat Cross-Platform

## 📋 **Overview**

This document defines the standardized naming conventions for analytics events and properties across all three BattleBoat platforms (Web, iOS, Android). These standards ensure consistency and make analytics data easier to analyze and compare.

## 🎯 **Naming Convention Rules**

### **Event Names**
- **Format**: `[Noun] [Past Tense Verb]`
- **Case**: Title Case
- **Examples**: 
  - ✅ "Ship Placed" (not "Place Ship")
  - ✅ "Game Started" (not "Start Game")
  - ✅ "Shot Fired" (not "Shoot Ship")

### **Property Names**
- **Case**: Title Case
- **Multi-word Properties**: Use quotes in JavaScript, natural names in other platforms
- **Examples**:
  - ✅ "Ship", "Success", "X", "Y"
  - ✅ "Shots Taken", "Consecutive Hits"

## 📊 **Standardized Events**

| Event Name | Properties | Platforms | Description |
|------------|------------|-----------|-------------|
| **Game Initialized** | - | Web | Game setup begins |
| **Game Started** | Platform, Version, Timestamp | All | Gameplay begins |
| **Game Ended** | Win, Shots Taken | All | Game finishes |
| **Ship Selected** | Ship, Ship Type | All | Player selects a ship to place |
| **Ship Placed** | Ship, Success, X, Y | All | Ship placement attempt |
| **Ship Rotated** | Ship | All | Ship orientation changed |
| **Shot Fired** | X, Y, Hit, Consecutive Hits, Player | All | Attack fired |
| **Ships Placed Randomly** | trigger_source, pre_placed_count | Web, iOS, Android | Random ship placement |
| **Tutorial Skipped** | - | iOS, Android | Player skips tutorial |
| **Tutorial Completed** | - | iOS, Android | Player completes tutorial |
| **Probability Heatmap Shown** | - | iOS | Heatmap displayed |
| **Probability Heatmap Hidden** | - | iOS | Heatmap hidden |
| **Statistics Reset** | - | iOS | Player resets stats |

## 🛠 **Implementation Examples**

### **Web (JavaScript)**
```javascript
amplitude.track('Ship Placed', {
    Ship: 'carrier',
    Success: true,
    X: 1,
    Y: 1
});

amplitude.track('Game Ended', {
    Win: true,
    "Shots Taken": 42
});
```

### **iOS (Swift)**
```swift
AnalyticsManager.shared.trackEvent(name: "Ship Placed", properties: [
    "Ship": "carrier",
    "Success": true,
    "X": 1,
    "Y": 1
])

AnalyticsManager.shared.trackGameEnd(playerWon: true, shotsTaken: 42)
```

### **Android (Kotlin)**
```kotlin
analyticsManager.trackPlaceShip("carrier", true, 1, 1)

analyticsManager.trackGameEnd(true, 42)
```

## 🔄 **Migration Summary**

### **Before → After**
- `Initialize Game` → `Game Initialized`
- `Start Game` → `Game Started`
- `Game Over` → `Game Ended` 
- `Select Ship` → `Ship Selected`
- `Place Ship` → `Ship Placed`
- `Rotate Ship` → `Ship Rotated`
- `Shoot Ship` → `Shot Fired`
- `Random Ship Placement` → `Ships Placed Randomly`

### **Property Standardization**
- `win` → `Win`
- `shotsTaken` → `Shots Taken`
- `ship` → `Ship`
- `success` → `Success`
- `x`, `y` → `X`, `Y`
- `hit` → `Hit`
- `consecutiveHits` → `Consecutive Hits`

## ✅ **Validation Checklist**

When adding new events or properties:

- [ ] Event name follows `[Noun] [Past Tense Verb]` format
- [ ] Event name is in Title Case
- [ ] All properties are in Title Case
- [ ] Same properties are used across all platforms for identical events
- [ ] Multi-word properties use appropriate syntax for each platform
- [ ] Event is documented in this standards file

## 🚀 **Benefits**

1. **Consistency**: Same event names and properties across all platforms
2. **Clarity**: Intuitive naming makes analytics data self-documenting
3. **Analysis**: Easier to create cross-platform reports and comparisons
4. **Maintenance**: Clear standards reduce confusion and errors

## 📝 **Maintenance**

- Update this document when adding new events
- Validate all new analytics implementations against these standards
- Review existing events quarterly to ensure continued compliance
- Use this document as the source of truth for all analytics naming decisions

---

**Last Updated**: August 2025  
**Version**: 1.0  
**Platforms**: Web (JavaScript), iOS (Swift), Android (Kotlin) 