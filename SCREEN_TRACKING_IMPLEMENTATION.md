# Screen Tracking Implementation for Amplitude Guides and Surveys

This document describes the screen tracking implementation added to both iOS and Android versions of the Battleboat app, enabling Amplitude Guides and Surveys to target users based on which screen they're viewing.

## Overview

Screen tracking has been implemented using the Amplitude Engagement SDK's `screen()` method, which allows Guides and Surveys to be displayed contextually based on the user's current screen.

Reference: [Amplitude Guides and Surveys iOS SDK Documentation](https://amplitude.com/docs/guides-and-surveys/guides-and-surveys-ios-sdk#enable-screen-tracking-optional)

## Screen Names Used

Both iOS and Android apps use **consistent screen naming** to ensure cross-platform analytics alignment:

### Main Screens

| Screen Name | Description | When Tracked |
|------------|-------------|--------------|
| `MainMenuScreen` | Main menu/home screen | When MainActivity opens (Android only) |
| `GameScreen` | Main game view | When GameViewController/GameActivity appears |

### Game State Screens

| Screen Name | Description | When Tracked |
|------------|-------------|--------------|
| `ShipPlacementScreen` | Ship placement phase | When game enters SETUP/placingShips state |
| `ReadyToPlayScreen` | Ready to start | When all ships placed (iOS only) |
| `PlayerTurnScreen` | Player's turn to shoot | When game enters PLAYER_TURN/playerTurn state |
| `AITurnScreen` | AI's turn to shoot | When game enters AI_TURN/computerTurn state |
| `ComputerTurnScreen` | Computer's turn (iOS) | When game enters computerTurn state (iOS only) |
| `GameOverScreen` | Game ended | When game enters GAME_OVER/gameOver state |

## Implementation Details

### iOS Implementation

**Files Modified:**
- `BattleboatIOS/BattleboatIOS/AnalyticsManager.swift`
- `BattleboatIOS/BattleboatIOS/GameViewController.swift`

**New Method Added:**
```swift
/// Track screen views for Guides and Surveys targeting
func trackScreen(name: String) {
    amplitudeEngagement.screen(name)
    print("📱 Screen tracked: \(name)")
}
```

**Screen Tracking Locations:**
1. **GameScreen** - Tracked in `viewDidAppear()`
2. **State-based screens** - Tracked in `gameModel(_:didUpdateState:)` delegate method based on `GameConstants.GameState`

### Android Implementation

**Files Modified:**
- `BattleboatAndroid/app/src/main/java/com/battleboat/AnalyticsManager.kt`
- `BattleboatAndroid/app/src/main/java/com/battleboat/MainActivity.kt`
- `BattleboatAndroid/app/src/main/java/com/battleboat/GameActivity.kt`

**New Method Added:**
```kotlin
/**
 * Track screen views for Guides and Surveys targeting
 */
fun trackScreen(screenName: String) {
    if (!isAnalyticsEnabled() || !isAmplitudeInitialized()) {
        return
    }
    
    try {
        amplitudeEngagement?.screen(screenName)
        Log.d(TAG, "📱 Screen tracked: $screenName")
    } catch (e: Exception) {
        Log.e(TAG, "❌ Failed to track screen: $screenName", e)
    }
}
```

**Screen Tracking Locations:**
1. **MainMenuScreen** - Tracked in `MainActivity.onCreate()`
2. **GameScreen** - Tracked in `GameActivity.onCreate()`
3. **ShipPlacementScreen** - Tracked in `startShipPlacement()`
4. **PlayerTurnScreen** - Tracked when `gameState = GameState.PLAYER_TURN`
5. **AITurnScreen** - Tracked when `gameState = GameState.AI_TURN`
6. **GameOverScreen** - Tracked in `endGame()`

## Usage in Amplitude Dashboard

When creating Guides or Surveys in the Amplitude dashboard, you can now target users based on these screen names:

### Example Targeting Rules

1. **Welcome Guide on Main Menu:**
   - Screen: `MainMenuScreen`
   - Use case: Show tutorial to new users when they first open the app

2. **Ship Placement Tips:**
   - Screen: `ShipPlacementScreen`
   - Use case: Help users understand how to place ships strategically

3. **Shooting Strategy Guide:**
   - Screen: `PlayerTurnScreen`
   - Use case: Provide tips on shooting patterns and AI behavior

4. **Post-Game Survey:**
   - Screen: `GameOverScreen`
   - Use case: Collect feedback after players complete a game

5. **AI Turn Information:**
   - Screen: `AITurnScreen` or `ComputerTurnScreen`
   - Use case: Explain what the AI is doing while waiting

## Testing Screen Tracking

To verify screen tracking is working:

1. **Check Logs:**
   - iOS: Look for "📱 Screen tracked: [ScreenName]" in Xcode console
   - Android: Look for "📱 Screen tracked: [ScreenName]" in Logcat

2. **Amplitude Dashboard:**
   - Navigate to Events in your Amplitude project
   - Look for screen view events with the screen names listed above

3. **Preview Mode:**
   - Use Amplitude's preview mode (configured via URL scheme)
   - Create a test Guide targeting a specific screen
   - Navigate to that screen in the app to see the Guide appear

## Notes

- Screen tracking is automatically enabled when the Amplitude Engagement SDK is initialized
- All screen names are case-sensitive
- Screen tracking calls are lightweight and don't impact app performance
- iOS includes additional state tracking for `ReadyToPlayScreen` and `ComputerTurnScreen` not present in Android

## Related Documentation

- [Amplitude Guides and Surveys iOS SDK](https://amplitude.com/docs/guides-and-surveys/guides-and-surveys-ios-sdk)
- [Amplitude Guides and Surveys Android SDK](https://amplitude.com/docs/guides-and-surveys/guides-and-surveys-android-sdk)
- Main analytics naming standards: `ANALYTICS_NAMING_STANDARDS.md`
- Complete events list: `COMPLETE_EVENTS_LIST.md`

