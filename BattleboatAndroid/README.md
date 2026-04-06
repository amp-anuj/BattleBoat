# Battleboat Android

A full Android implementation of the Battleship game, used as a test bed for Amplitude analytics instrumentation on Android.

## Amplitude Instrumentation

### SDKs Integrated

| SDK | Version | Purpose |
|-----|---------|---------|
| `com.amplitude:android-kotlin` | 1.22.0+ | Core analytics and event tracking |
| `com.amplitude:engagement-android` | latest | Guides & Surveys |
| `com.amplitude:plugin-network-tracking` | latest | Network request tracking |

> Note: Session Replay is currently disabled due to known reflection access issues with Jetpack Compose Alpha. Waiting for stable Compose support from Amplitude.

### Features

**Analytics**
- Custom game events (see [Events](#events) below)
- Autocapture: sessions, app lifecycle, frustration interactions
- User and device ID management with SharedPreferences persistence

**Guides & Surveys**
- `AmplitudeEngagement` initialized and booted with device ID
- Screen tracking for guide/survey targeting (`amplitudeEngagement.screen(name)`)
- **Custom callback**: `place_ship` — triggered by a Guide to programmatically place the carrier ship in the game
- Retry logic for JS engine readiness

**Network Tracking**
- `NetworkTrackingPlugin` added as an OkHttp interceptor
- Tracks 4xx/5xx responses from configured hosts
- Configured to capture Amplitude API requests

**WebView Identity Linking**
- Native device ID, user ID, and session ID are exposed via `getDeviceId()`, `getUserId()`, `getSessionId()` on `AnalyticsManager`
- These are passed as URL params when loading the web app in a WebView, stitching native and web sessions together

### Key File

`app/src/main/java/com/battleboat/AnalyticsManager.kt` — singleton managing all Amplitude initialization, event tracking, and SDK coordination.

---

## Events

| Event | Trigger |
|-------|---------|
| `Game Started` | Player starts a new game |
| `Game Ended` | Game over; includes `Win` (bool), `Shots Taken` (int) |
| `Shot Fired` | Any shot; includes `X`, `Y`, `Hit`, `Player`, `Consecutive Hits` |
| `Ship Selected` | Player selects a ship to place; includes `Ship` |
| `Ship Placed` | Ship placed on grid; includes `Ship`, `X`, `Y`, `Success` |
| `Ship Rotated` | Player rotates selected ship |

---

## Setup

### Requirements
- Android Studio Hedgehog (2023.1.1) or newer
- Android SDK API 24+ (Android 7.0)
- Kotlin support

### Build and Run

1. Open Android Studio and select **Open an existing project**
2. Navigate to `BattleboatAndroid/` and open it
3. Click **Sync Now** when prompted
4. Click the green Run button or press Shift+F10

### API Key

The Amplitude API key is set in `AnalyticsManager.kt`:

```kotlin
private const val API_KEY = "your_api_key_here"
```

All platforms in this repo share the same Amplitude project. To point to your own project, replace the key here.

### Command Line

```bash
cd BattleboatAndroid
./gradlew installDebug   # Build and install on connected device/emulator
adb logcat | grep AnalyticsManager   # View analytics debug logs
```

---

## Project Structure

```
BattleboatAndroid/
├── app/src/main/java/com/battleboat/
│   ├── AnalyticsManager.kt    # All Amplitude SDK setup and event tracking
│   ├── MainActivity.kt        # Main menu; initializes AnalyticsManager
│   ├── GameActivity.kt        # Gameplay screen; fires game events
│   ├── WebViewActivity.kt     # Loads web app with identity linking
│   ├── AI.kt                  # Probability-based computer opponent
│   ├── Grid.kt                # 10x10 game board
│   ├── Fleet.kt               # Ship collection management
│   ├── Ship.kt                # Individual ship logic
│   ├── GameStats.kt           # Persistent stats (SharedPreferences)
│   └── GameConstants.kt       # Enums and constants
├── app/build.gradle           # SDK dependencies
└── README.md
```

---

## Guides & Surveys Callback Example

The `place_ship` callback is registered in `AnalyticsManager.kt` and triggered from an Amplitude Guide to take a real action inside the app:

```kotlin
amplitudeEngagement?.addCallback("place_ship") {
    val carrierShip = activity.getPlayerFleet().getShip(ShipType.CARRIER)
    val success = activity.getPlayerFleet().placeShip(carrierShip, row=1, col=1, Orientation.HORIZONTAL)
    if (success) {
        activity.getPlayerGrid().placeShip(carrierShip)
        activity.refreshUI()
    }
}
```

This pattern lets you build interactive onboarding guides that drive real in-app behavior.

---

## Game Overview

Mirrors the iOS and web implementations:
- 10x10 grids for player and computer
- 5 ship types: Carrier (5), Battleship (4), Destroyer (3), Submarine (3), Patrol Boat (2)
- Probability-based AI with hunt mode and directional targeting
- Persistent game statistics

Original game by [Bill Mei](https://github.com/billmei/battleboat).
