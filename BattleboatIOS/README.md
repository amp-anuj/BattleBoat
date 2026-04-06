# Battleboat iOS

A full iOS implementation of the Battleship game, used as a test bed for Amplitude analytics instrumentation on iOS.

## Amplitude Instrumentation

This app demonstrates the most complete set of Amplitude features across all platforms in this repo.

### SDKs Integrated

| SDK | Version | Purpose |
|-----|---------|---------|
| `Amplitude-Swift` | 1.15.0+ | Core analytics and event tracking |
| `AmplitudeEngagementSwift` | 3.0.0+ | Guides & Surveys |
| `AmplitudeSessionReplay` | latest | Session Replay |
| `Experiment` | latest | Feature flags and A/B testing |

### Features

**Analytics**
- Custom game events (see [Events](#events) below)
- User property tracking (win rate, accuracy, games played)
- Autocapture: sessions, app lifecycle, frustration interactions
- Network tracking

**Session Replay**
- 100% sample rate (`sampleRate: 1.0`)
- Conservative mask level
- WebView capture enabled (`captureWebViews: true`)
- Remote config enabled

**Guides & Surveys**
- Initialized via `AmplitudeEngagementFactory.make`
- Booted with device ID + user ID for targeting
- Event forwarding: Guides & Surveys events forwarded to Amplitude Analytics
- Screen tracking for guide/survey targeting
- **Custom callback**: `place_ship` — triggered by a Guide to programmatically place the carrier ship in the game

**Experiment**
- Initialized via `Experiment.initializeWithAmplitudeAnalytics`
- Manual exposure tracking (`automaticExposureTracking: false`)
- Flag `battleboat-test` — variant is attached to `Ship Selected` events

**WebView Identity Linking**
- Native device ID, user ID, and session ID are passed to the in-app WebView as URL params (`amp_device_id`, `amp_user_id`, `amp_session_id`)
- The web app reads these params and initializes Amplitude with the same identity, stitching native and web sessions together

### Key File

`BattleboatIOS/AnalyticsManager.swift` — singleton managing all Amplitude initialization, event tracking, and SDK coordination.

---

## Events

| Event | Trigger |
|-------|---------|
| `Game Started` | Player taps Start Game after placing ships |
| `Game Ended` | Game over; includes `Win` (bool), `Shots Taken` (int) |
| `Shot Fired` | Any shot; includes `X`, `Y`, `Hit`, `Player`, `Consecutive Hits` |
| `Ship Selected` | Player selects a ship to place; includes Experiment variant |
| `Ship Placed` | Ship placed on grid; includes `Ship`, `X`, `Y`, `Success` |
| `Ship Rotated` | Player rotates selected ship |
| `Tutorial Step` | Each tutorial step reached |
| `Tutorial Skipped` / `Tutorial Completed` | Tutorial outcome |
| `Ships Placed Randomly` | Player uses random placement button |
| `Probability Heatmap Shown/Hidden` | AI targeting heatmap toggled |
| `Human Grid Analysis` | Ship placement positions recorded at game end |

---

## Setup

### Requirements
- Xcode 15.0+
- iOS 17.0+
- Swift 5.9+

### Build and Run

```bash
cd BattleboatIOS
open BattleboatIOS.xcodeproj
```

Select a simulator or device and press Cmd+R.

### API Key

The Amplitude API key is set in `BattleboatIOS/AnalyticsManager.swift`:

```swift
private let amplitudeAPIKey = "your_api_key_here"
```

All platforms in this repo share the same Amplitude project. To point to your own project, replace the key and update the Guides & Surveys URL scheme in `Info.plist` (`CFBundleURLSchemes`).

---

## Project Structure

```
BattleboatIOS/
├── BattleboatIOS/
│   ├── AnalyticsManager.swift    # All Amplitude SDK setup and event tracking
│   ├── GameViewController.swift  # Main game UI; drives AnalyticsManager calls
│   ├── WebViewController.swift   # Loads web app with identity linking
│   ├── GameModel.swift           # Core game logic and state machine
│   ├── AI.swift                  # Probability-based computer opponent
│   ├── Grid.swift                # 10x10 game board
│   ├── Fleet.swift               # Ship collection management
│   ├── Ship.swift                # Individual ship logic
│   ├── GridView.swift            # Game board UI component
│   ├── GameStats.swift           # Persistent stats (UserDefaults)
│   └── GameConstants.swift       # Enums and constants
└── README.md
```

---

## Guides & Surveys Callback Example

The `place_ship` callback is registered in `AnalyticsManager.swift` and triggered from an Amplitude Guide to take a real action inside the app:

```swift
amplitudeEngagement.addCallback("place_ship") {
    // Selects carrier, sets horizontal orientation, places at (1,1)
    gameModel.selectShip(type: .carrier)
    if gameModel.selectedShipDirection != .horizontal {
        gameModel.rotateSelectedShip()
    }
    let success = gameModel.placeShip(at: 1, y: 1)
}
```

This pattern lets you build interactive onboarding guides that drive real in-app behavior.

---

## Game Overview

The game faithfully recreates the Battleboat.js web experience on iOS:
- 10x10 grids for player and computer
- 5 ship types: Carrier (5), Battleship (4), Destroyer (3), Submarine (3), Patrol Boat (2)
- Probability-based AI with hunt mode and directional targeting
- Tutorial system for new players
- AI probability heatmap visualization
- Persistent game statistics

Original game by [Bill Mei](https://github.com/billmei/battleboat).
