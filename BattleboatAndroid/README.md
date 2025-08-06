# 🚢 Battleboat Android

A complete Android implementation of the Battleboat game with intelligent AI opponent.

## Features

✅ **Complete Game Engine** - All core game logic (Ship, Grid, Fleet, AI, GameStats)  
✅ **Intelligent AI** - Probability-based targeting with hunt mode and directional shooting  
✅ **Modern Android UI** - Material Design 3 with proper theming and resources  
✅ **Analytics Integration** - Amplitude SDK ready (add your API key)  
✅ **Statistics Tracking** - Persistent game stats with SharedPreferences  
✅ **Tutorial System** - Built-in tutorial for new players  
✅ **Multi-language Ready** - Structured string resources  

## Quick Start

### Option 1: Android Studio (Recommended)

1. **Open in Android Studio**
   ```bash
   # Open Android Studio and select "Open an existing project"
   # Navigate to the BattleboatAndroid folder and select it
   ```

2. **Sync Project**
   - Android Studio will automatically detect the Gradle project
   - Click "Sync Now" when prompted
   - Wait for dependencies to download

3. **Add Analytics (Optional)**
   - Open `app/src/main/java/com/battleboat/AnalyticsManager.kt`
   - Replace `YOUR_AMPLITUDE_API_KEY_HERE` with your actual Amplitude API key
   - Or leave as-is to disable analytics

4. **Build and Run**
   - Click the green play button or press `Shift + F10`
   - Select an emulator or connected device
   - The app will build and launch automatically

### Option 2: Command Line

1. **Install Android SDK** (if not already installed)
   ```bash
   # Download Android Studio or Android SDK command-line tools
   # Set ANDROID_HOME environment variable
   ```

2. **Initialize Gradle Wrapper**
   ```bash
   cd BattleboatAndroid
   gradle wrapper
   ```

3. **Build the Project**
   ```bash
   ./gradlew build
   ```

4. **Install on Device**
   ```bash
   ./gradlew installDebug
   ```

## Project Structure

```
BattleboatAndroid/
├── app/
│   ├── src/main/
│   │   ├── java/com/battleboat/
│   │   │   ├── GameConstants.kt      # Game enums and constants
│   │   │   ├── Ship.kt              # Individual ship logic
│   │   │   ├── Grid.kt              # 10x10 game board
│   │   │   ├── Fleet.kt             # Ship collection management
│   │   │   ├── AI.kt                # Intelligent AI opponent
│   │   │   ├── GameStats.kt         # Statistics tracking
│   │   │   ├── AnalyticsManager.kt  # Amplitude integration
│   │   │   ├── MainActivity.kt      # Main menu screen
│   │   │   └── GameActivity.kt      # Game play screen
│   │   └── res/
│   │       ├── layout/              # UI layouts
│   │       ├── values/              # Strings, colors, themes
│   │       └── drawable/            # UI graphics
│   └── build.gradle                 # App-level dependencies
├── build.gradle                     # Root project configuration
├── settings.gradle                  # Project settings
└── gradlew                         # Gradle wrapper script
```

## Game Architecture

### Core Components

- **GameConstants**: All game enums (ShipType, CellType, GameState, etc.)
- **Ship**: Individual ship with placement validation and damage tracking
- **Grid**: 10x10 game board with cell state management
- **Fleet**: Collection of ships with placement logic
- **AI**: Sophisticated opponent with probability-based targeting
- **GameStats**: Persistent statistics with achievements

### AI Intelligence

The AI implements advanced battleship strategies:

- **Probability Grid**: Calculates likelihood of ship placement for each cell
- **Hunt Mode**: When a ship is hit, systematically searches adjacent cells
- **Directional Targeting**: Once direction is found, continues in that line
- **Strategic Bonuses**: Center bias, checkerboard patterns, and difficulty scaling

### Analytics

Comprehensive event tracking for user engagement:

- Game lifecycle (start, end, duration)
- Player actions (shots, hits, ship placement)
- Tutorial progress and completion
- Settings changes and user preferences
- Performance metrics and error tracking

## Configuration

### Analytics Setup

1. **Get Amplitude API Key**
   - Sign up at [amplitude.com](https://amplitude.com)
   - Create a new project
   - Copy your API key

2. **Update AnalyticsManager**
   ```kotlin
   // In AnalyticsManager.kt, replace:
   private const val API_KEY = "YOUR_AMPLITUDE_API_KEY_HERE"
   // With:
   private const val API_KEY = "your_actual_api_key"
   ```

3. **Privacy Compliance**
   - Analytics are opt-in by default
   - Users can disable in settings
   - No personal data is collected

### Customization

- **Difficulty**: Modify AI difficulty in `AIDifficulty` enum
- **Grid Size**: Change `GRID_SIZE` in `GameConstants` (currently 10x10)
- **Ship Types**: Add/modify ships in `ShipType` enum
- **UI Themes**: Customize colors and styles in `res/values/`

## Dependencies

- **Kotlin** 1.9.10
- **Android Gradle Plugin** 8.1.2
- **Material Design** 1.10.0
- **Amplitude Analytics** 1.14.0
- **AndroidX Libraries** (Core, AppCompat, ConstraintLayout, etc.)

## Minimum Requirements

- **Android API 24** (Android 7.0)
- **Target API 34** (Android 14)
- **Kotlin** support
- **64MB RAM** minimum for gameplay

## Build Variants

- **Debug**: Development build with logging
- **Release**: Optimized production build

## Contributing

This is a complete, standalone implementation. To extend:

1. **Enhanced UI**: Add custom grid views with animations
2. **Multiplayer**: Network play between devices
3. **AI Variations**: Multiple AI personalities
4. **Ship Customization**: Different ship sets and sizes
5. **Visual Effects**: Explosions, water splashes, etc.

## Troubleshooting

### Common Issues

1. **Build Fails**
   - Ensure Android SDK is installed and `ANDROID_HOME` is set
   - Check internet connection for dependency downloads
   - Try "Clean Project" in Android Studio

2. **App Crashes**
   - Check device logs: `adb logcat | grep Battleboat`
   - Ensure minimum API level (24) is met
   - Verify app permissions in device settings

3. **Analytics Not Working**
   - Verify API key is correctly set
   - Check network connectivity
   - Ensure analytics opt-in is enabled

### Performance Optimization

- The AI calculations are optimized for mobile devices
- Game state is efficiently managed with minimal memory usage
- UI updates are throttled to maintain smooth gameplay

## License

Open source implementation of classic Battleship game mechanics.

---

🎯 **Ready for Battle!** 🚢

This Android version perfectly matches the iOS implementation with the same intelligent AI, game mechanics, and user experience. Open in Android Studio and start playing! 