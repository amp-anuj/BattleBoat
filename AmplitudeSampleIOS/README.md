# Amplitude Sample iOS App

A minimal iOS sample app demonstrating the installation and setup of **Amplitude Analytics** and **Guides & Surveys** SDK.

## Overview

This sample app provides a clean, shareable example of how to integrate Amplitude into an iOS application. It includes:

- ✅ **Amplitude Analytics SDK** - Event tracking and user identification
- ✅ **Amplitude Guides & Surveys SDK** - In-app guides and surveys
- ✅ **Screen Tracking** - For guide/survey targeting
- ✅ **Event Forwarding** - Guides & Surveys events sent to Amplitude
- ✅ **URL Handling** - For preview mode testing

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
| Amplitude-Swift | 1.15.0+ |
| Amplitude-Engagement-Swift | 1.7.0+ |

## Project Structure

```
AmplitudeSampleIOS/
├── AmplitudeSampleIOS.xcodeproj    # Xcode project
├── AmplitudeSampleIOS/
│   ├── AppDelegate.swift           # App entry point, initializes Amplitude
│   ├── SceneDelegate.swift         # Scene management, URL handling
│   ├── ViewController.swift        # Main view, demonstrates screen tracking
│   ├── AmplitudeManager.swift      # ⭐ Core Amplitude implementation
│   └── Info.plist                  # App configuration with URL scheme
└── README.md                       # This file
```

## Key File: AmplitudeManager.swift

The `AmplitudeManager.swift` file contains the complete Amplitude setup:

```swift
import AmplitudeSwift
import AmplitudeEngagementSwift

class AmplitudeManager {
    static let shared = AmplitudeManager()
    
    private let apiKey = "YOUR_API_KEY"
    private var amplitude: Amplitude!
    private var amplitudeEngagement: AmplitudeEngagement!
    
    func initialize() {
        // Step 1: Initialize Guides & Surveys
        amplitudeEngagement = AmplitudeEngagement(apiKey)
        
        // Step 2: Initialize Analytics
        amplitude = Amplitude(configuration: Configuration(
            apiKey: apiKey,
            logLevel: LogLevelEnum.DEBUG
        ))
        
        // Step 3: Add Guides & Surveys plugin
        amplitude.add(plugin: amplitudeEngagement.getPlugin())
        
        // Step 4: Boot with device ID and event forwarding
        let bootOptions = AmplitudeBootOptions(
            user_id: "",
            device_id: amplitude.getDeviceId() ?? "",
            integrations: [
                { event, eventProperties in
                    let baseEvent = BaseEvent(
                        eventType: event,
                        eventProperties: eventProperties
                    )
                    self.amplitude.track(event: baseEvent)
                }
            ]
        )
        amplitudeEngagement.boot(options: bootOptions)
    }
    
    func trackScreen(name: String) {
        amplitudeEngagement.screen(name)
    }
    
    func handleUrl(_ url: URL) -> Bool {
        return amplitudeEngagement.handleUrl(url)
    }
}
```

## Usage

### Initialize on App Launch

```swift
// In AppDelegate.swift
func application(_ application: UIApplication, 
                 didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
    AmplitudeManager.shared.initialize()
    return true
}
```

### Track Screen Views

```swift
// In any ViewController
override func viewDidAppear(_ animated: Bool) {
    super.viewDidAppear(animated)
    AmplitudeManager.shared.trackScreen(name: "HomeScreen")
}
```

### Handle Preview URLs

```swift
// In SceneDelegate.swift
func scene(_ scene: UIScene, openURLContexts URLContexts: Set<UIOpenURLContext>) {
    guard let url = URLContexts.first?.url else { return }
    _ = AmplitudeManager.shared.handleUrl(url)
}
```

## URL Scheme for Preview

The app is configured with URL scheme `amp-5ca0d2531a1b801e` for Amplitude Guides & Surveys preview testing.

To add your own URL scheme:
1. Open `Info.plist`
2. Find `CFBundleURLSchemes`
3. Replace with your Amplitude project's URL scheme (found in Project Settings → Guides and Surveys)

## Documentation

- [Amplitude iOS Swift SDK](https://amplitude.com/docs/sdks/analytics/ios/ios-swift-sdk)
- [Guides & Surveys iOS SDK](https://amplitude.com/docs/guides-and-surveys/guides-and-surveys-ios-sdk)

## License

MIT

