# Battleboat iOS

A complete iOS recreation of the popular Battleboat.js web game - an intelligent AI-powered Battleship game that beats humans.

## Overview

This iOS app faithfully recreates all the features from the original JavaScript web game:

- **Intelligent AI Opponent**: Advanced probability-based AI that learns and adapts
- **Interactive Ship Placement**: Drag, drop, and rotate ships on your grid  
- **Tutorial System**: Guided tutorial for new players
- **Statistics Tracking**: Comprehensive win/loss and accuracy statistics
- **Probability Heatmap**: Visualize the AI's targeting strategy
- **Analytics Integration**: Amplitude tracking for user engagement insights
- **Modern iOS Interface**: Native UIKit interface optimized for iPhone and iPad

## Features

### Core Game Features
- ✅ 10x10 game grids for player and computer
- ✅ 5 ship types: Carrier (5), Battleship (4), Destroyer (3), Submarine (3), Patrol Boat (2)
- ✅ Ship placement with rotation and validation
- ✅ Turn-based gameplay with immediate feedback
- ✅ Visual hit/miss/sunk indicators

### AI Features  
- ✅ Probability-based ship targeting
- ✅ Hunt mode when ships are hit
- ✅ Directional targeting for ship destruction
- ✅ Advanced heuristics for optimal play
- ✅ Debug mode with probability visualization

### UI/UX Features
- ✅ Responsive grid-based interface
- ✅ Animated interactions and feedback
- ✅ Tutorial overlay system
- ✅ Statistics display and persistence
- ✅ Settings and preferences
- ✅ Support for both iPhone and iPad
- ✅ Dark mode compatibility
- ✅ Comprehensive analytics tracking with Amplitude

## Technical Architecture

### Core Classes

#### `GameModel`
- Central game state management
- Coordinates all game components
- Handles game flow and rules
- Manages tutorial progression

#### `Grid`
- Represents 10x10 game board
- Handles cell state management
- Provides validation and utilities
- Supports shooting and ship placement

#### `Ship`
- Individual ship representation
- Placement validation and logic
- Damage tracking and sunk detection
- Coordinate management

#### `Fleet`
- Collection of ships for each player
- Fleet-wide operations and validation
- Random ship placement algorithms
- Win/loss condition checking

#### `AI`
- Intelligent computer opponent
- Probability-based targeting system
- Hunt mode for ship destruction
- Adaptive learning algorithms

#### `GameStats`
- Persistent statistics tracking
- UserDefaults integration
- Performance level calculation
- Win/loss ratio analysis

### UI Components

#### `GridView`
- Visual representation of game grids
- Touch handling for ship placement/shooting
- Cell animations and visual feedback
- Probability heatmap display

#### `GameViewController`
- Main game interface controller
- UI state management
- User interaction handling
- Tutorial presentation

## Installation & Setup

### Requirements
- Xcode 15.0+
- iOS 17.0+
- Swift 5.9+

### Building the App

1. **Clone or download the project files**
   ```bash
   cd BattleboatIOS
   ```

2. **Open in Xcode**
   ```bash
   open BattleboatIOS.xcodeproj
   ```

3. **Select target device/simulator**
   - Choose iPhone or iPad simulator
   - Or connect physical device

4. **Configure Analytics (Optional)**
   - See `ANALYTICS_SETUP.md` for Amplitude integration
   - Add your API key to `AnalyticsManager.swift`

5. **Build and run**
   - Press Cmd+R or click the Run button
   - App will build and launch automatically

### Project Structure
```
BattleboatIOS/
├── BattleboatIOS/
│   ├── AppDelegate.swift          # App lifecycle management
│   ├── SceneDelegate.swift        # Scene management (iOS 13+)
│   ├── GameViewController.swift   # Main game interface
│   ├── GameModel.swift           # Core game logic
│   ├── GameConstants.swift       # Game constants and enums
│   ├── Grid.swift               # Game board representation
│   ├── Ship.swift              # Individual ship logic
│   ├── Fleet.swift             # Ship collection management
│   ├── AI.swift               # Computer opponent AI
│   ├── GameStats.swift        # Statistics tracking
│   ├── GridView.swift         # Visual grid component
│   ├── AnalyticsManager.swift # Amplitude analytics integration
│   └── Info.plist            # App configuration
└── README.md                 # This file
```

## How to Play

### Ship Placement Phase
1. **Select a ship** from the list on the left
2. **Tap on your grid** to place the ship
3. **Use the Rotate button** to change ship orientation
4. **Repeat** until all 5 ships are placed
5. **Tap Start Game** to begin battle

### Battle Phase
1. **Tap on the enemy grid** to fire shots
2. **Hit**: Red X indicates a hit on enemy ship
3. **Miss**: White circle indicates a miss
4. **Sunk**: Red border indicates a completely destroyed ship
5. **Continue** until all enemy ships are destroyed or yours are sunk

### Advanced Features
- **Show AI Heatmap**: Visualize the computer's targeting probabilities
- **Statistics**: Track your performance over multiple games
- **Tutorial**: Follow guided instructions for new players

## Original Game Credits

This iOS app is based on the Battleboat.js web game:
- **Original Creator**: Bill Mei
- **Original Repository**: https://github.com/billmei/battleboat
- **Web Version**: https://billmei.github.io/battleboat

## AI Algorithm

The AI uses sophisticated algorithms similar to the original:

1. **Probability Calculation**: Calculate likelihood of ship placement for each cell
2. **Search Mode**: Use probability grid to find ships
3. **Hunt Mode**: When a ship is hit, target adjacent cells systematically
4. **Destroy Mode**: Follow ship direction to sink completely
5. **Learning**: Adapt strategy based on game progress

The AI typically achieves 80%+ win rates against human players.

## Development Notes

### Key Implementation Details
- **Model-View-Controller Architecture**: Clean separation of game logic and UI
- **Delegate Pattern**: Loose coupling between game model and view controller
- **Auto Layout**: Responsive design for all screen sizes
- **UserDefaults**: Persistent storage for statistics and preferences
- **Animations**: Smooth UI transitions and feedback
- **Accessibility**: VoiceOver support for visually impaired users

### Performance Optimizations
- **Efficient Grid Updates**: Only redraw changed cells
- **AI Calculation Caching**: Optimize probability calculations
- **Memory Management**: Proper object lifecycle management
- **Background Processing**: Non-blocking AI computations

## License

This project is open source and available under the MIT License, following the original Battleboat.js project.

## Contributing

Contributions are welcome! Please feel free to submit pull requests or open issues for bugs and feature requests.

---

**Enjoy playing Battleboat iOS!** 🚢⚔️ 