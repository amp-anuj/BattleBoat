# Amplitude Sample iOS App

A minimal iOS sample app demonstrating the installation and setup of **Amplitude Analytics** and **Guides & Surveys** SDK.

This is the simplest entry point in the [Battleboat test bed](../README.md) — no game logic, no extra features. Use it as a clean reference when you just need to see how to wire up the SDKs from scratch.

## Role in the Test Bed

| App | Complexity | Use when... |
|-----|-----------|-------------|
| **This app** | Minimal | You need a clean SDK setup reference |
| `BattleboatIOS` | Full game | You need full instrumentation with Session Replay, Experiment, callbacks, and WebView linking |

## What's Included

- **Amplitude Analytics SDK** — event tracking and user identification
- **Amplitude Guides & Surveys SDK** — in-app guides and surveys
- Screen tracking for guide/survey targeting
- Event forwarding (Guides & Surveys → Analytics)
- URL scheme handling for Guides & Surveys preview mode

## Requirements

- iOS 15.0+
- Xcode 15.0+
- Swift 5.9+

## Installation

1. Open `AmplitudeSampleIOS.xcodeproj` in Xcode
2. Wait for Swift Package Manager to resolve dependencies
3. Build and run on a simulator or device

## SDK Versions

| SDK | Version |
|-----|---------|
| `Amplitude-Swift` | 1.15.0+ |
| `AmplitudeEngagementSwift` | 1.7.0+ |

## Project Structure

```
AmplitudeSampleIOS/
├── AmplitudeSampleIOS.xcodeproj
├── AmplitudeSampleIOS/
│   ├── AppDelegate.swift           # Calls AmplitudeManager.shared.initialize() on launch
│   ├── SceneDelegate.swift         # Handles URL scheme for Guides & Surveys preview
│   ├── ViewController.swift        # Tracks screen name via AmplitudeManager
│   └── AmplitudeManager.swift      # Core SDK setup — the key file to reference
└── README.md
```

## Core Setup (AmplitudeManager.swift)

```swift
import AmplitudeSwift
import AmplitudeEngagementSwift

class AmplitudeManager {
    static let shared = AmplitudeManager()
    private let apiKey = "YOUR_API_KEY"

    func initialize() {
        // 1. Initialize Guides & Surveys
        let amplitudeEngagement = AmplitudeEngagement(apiKey)

        // 2. Initialize Analytics
        let amplitude = Amplitude(configuration: Configuration(apiKey: apiKey))

        // 3. Connect Guides & Surveys to Analytics
        amplitude.add(plugin: amplitudeEngagement.getPlugin())

        // 4. Boot with device ID and event forwarding
        let bootOptions = AmplitudeBootOptions(
            user_id: "",
            device_id: amplitude.getDeviceId() ?? "",
            integrations: [{ event, props in
                amplitude.track(event: BaseEvent(eventType: event, eventProperties: props))
            }]
        )
        amplitudeEngagement.boot(options: bootOptions)
    }
}
```

## Usage

**Initialize on launch** (AppDelegate.swift):
```swift
AmplitudeManager.shared.initialize()
```

**Track screen views** (any ViewController):
```swift
override func viewDidAppear(_ animated: Bool) {
    super.viewDidAppear(animated)
    AmplitudeManager.shared.trackScreen(name: "HomeScreen")
}
```

**Handle Guides & Surveys preview URLs** (SceneDelegate.swift):
```swift
func scene(_ scene: UIScene, openURLContexts URLContexts: Set<UIOpenURLContext>) {
    guard let url = URLContexts.first?.url else { return }
    _ = AmplitudeManager.shared.handleUrl(url)
}
```

## URL Scheme for Preview Mode

The app is configured with URL scheme `amp-5ca0d2531a1b801e` for Guides & Surveys preview testing. To use your own:

1. Open `Info.plist`
2. Find `CFBundleURLSchemes`
3. Replace with your project's URL scheme (found in Amplitude → Project Settings → Guides and Surveys)

## Documentation

- [Amplitude iOS Swift SDK](https://amplitude.com/docs/sdks/analytics/ios/ios-swift-sdk)
- [Guides & Surveys iOS SDK](https://amplitude.com/docs/guides-and-surveys/guides-and-surveys-ios-sdk)
