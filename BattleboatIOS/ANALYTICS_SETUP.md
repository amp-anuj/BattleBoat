# Amplitude Analytics Setup Guide

This guide explains how to configure Amplitude analytics for the Battleboat iOS app.

## 🔑 Setting Up Your Amplitude API Key

### Step 1: Get Your Amplitude API Key

1. **Sign up for Amplitude** at [amplitude.com](https://amplitude.com) if you don't have an account
2. **Create a new project** for your Battleboat iOS app
3. **Copy your API key** from the project settings

### Step 2: Configure the API Key

1. Open `BattleboatIOS/AnalyticsManager.swift`
2. Find this line:
   ```swift
   private let amplitudeAPIKey = "YOUR_AMPLITUDE_API_KEY"
   ```
3. Replace `"YOUR_AMPLITUDE_API_KEY"` with your actual Amplitude API key:
   ```swift
   private let amplitudeAPIKey = "your-actual-api-key-here"
   ```

### Step 3: Test the Integration

1. Build and run the app
2. Check Xcode console for the message: `"Analytics initialized with Amplitude"`
3. Verify events appear in your Amplitude dashboard (may take a few minutes)

## 📊 Tracked Events

The app tracks the following events matching the original JavaScript version:

### Core Game Events
- `Initialize Game` - When a new game starts
- `Select Ship` - When player selects a ship to place
- `Place Ship` - When player attempts to place a ship (with success/failure)
- `Rotate Ship` - When player rotates a ship
- `Start Game` - When gameplay begins
- `Shoot Ship` - When player fires at enemy grid
- `Game Over` - When game ends (with win/loss and shots taken)

### Tutorial Events
- `Tutorial Step` - Each tutorial step progression
- `Tutorial Completed` - When tutorial is finished
- `Tutorial Skipped` - When player skips tutorial

### iOS-Specific Events
- `Random Ship Placement` - When using random placement feature
- `Show Probability Heatmap` - When AI heatmap is displayed
- `Hide Probability Heatmap` - When AI heatmap is hidden
- `Reset Statistics` - When stats are reset
- `App Launch` - App startup
- `App Background` / `App Foreground` - App lifecycle
- `Session Start` / `Session End` - Session tracking

### Analysis Events
- `Human Grid Analysis` - Ship placement patterns for AI improvement

## 👤 User Properties

The app sets these user properties for analytics:

### Game Statistics
- `games_played` - Total games played
- `games_won` - Total games won
- `win_percentage` - Win rate percentage
- `overall_accuracy` - Shot accuracy percentage
- `total_shots` - Total shots fired
- `total_hits` - Total hits achieved
- `performance_level` - Player skill level (Beginner, Novice, Intermediate, Advanced, Expert)

### Device Information
- `platform` - Always "iOS"
- `device_model` - iOS device model
- `ios_version` - iOS version
- `app_version` - App version

## 🔒 Privacy Considerations

### User Consent
The app automatically tracks analytics. Consider adding user consent for GDPR compliance:

```swift
// To opt out a user
AnalyticsManager.shared.optOut()

// To opt in a user
AnalyticsManager.shared.optIn()

// Check opt-out status
let isOptedOut = AnalyticsManager.shared.isOptedOut()
```

### Debug Mode
In debug builds, analytics are marked as debug mode:
```swift
#if DEBUG
AnalyticsManager.shared.enableDebugMode()
#endif
```

### Data Collected
The app collects:
- ✅ Game interaction events (anonymous)
- ✅ Device information (model, OS version)
- ✅ App performance metrics
- ❌ No personal information
- ❌ No location data
- ❌ No contact information

## 🛠 Advanced Configuration

### Custom Events
To add custom events, extend `AnalyticsManager`:

```swift
func trackCustomEvent(eventName: String, properties: [String: Any] = [:]) {
    let event = BaseEvent(
        eventType: eventName,
        eventProperties: properties
    )
    amplitude.track(event: event)
}
```

### Environment-Specific Configuration
Use different API keys for development and production:

```swift
private let amplitudeAPIKey: String = {
    #if DEBUG
    return "your-dev-api-key"
    #else
    return "your-prod-api-key"
    #endif
}()
```

### Disabling Analytics
To completely disable analytics in certain builds:

```swift
func configure() {
    #if DISABLE_ANALYTICS
    amplitude.configuration.optOut = true
    return
    #endif
    
    // Normal configuration...
}
```

## 📈 Analytics Dashboard

In your Amplitude dashboard, you can:

1. **Track user engagement** - See how often players play and for how long
2. **Analyze game balance** - Monitor win/loss rates and shot accuracy
3. **Improve AI** - Use ship placement data to enhance AI strategy
4. **Identify issues** - Find where players get stuck or quit
5. **A/B testing** - Test different game features or UI changes

## 🚀 Next Steps

1. **Set up your API key** as described above
2. **Deploy the app** and start collecting data
3. **Create dashboards** in Amplitude to monitor key metrics
4. **Set up alerts** for important events or issues
5. **Use insights** to improve gameplay and user experience

---

**Important**: Remember to update your App Store privacy labels to reflect the analytics data collection when submitting to the App Store. 