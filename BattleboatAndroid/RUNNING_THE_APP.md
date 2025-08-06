# Running the Android Battleboat App

The app has been successfully built and is ready to test! Here are your options:

## ✅ What's Fixed

1. **🎯 Interactive Grids**: Real 10x10 grids instead of blue boxes
2. **🚢 Ship Placement**: Ships are now visible on your grid
3. **💥 Click-to-Shoot**: Tap enemy grid cells to fire instead of auto-shooting
4. **📊 Analytics**: Amplitude analytics fully enabled and working
5. **🎨 Visual Feedback**: Hit/miss markers, ship visualization, coordinates

## 🚀 Running the App

### Option 1: Android Studio (Recommended)
1. Open Android Studio
2. Click "Open an existing project"
3. Navigate to `/Users/anuj.sharma/JS/battleboat-gh-pages/BattleboatAndroid`
4. Wait for Gradle sync to complete
5. Click the green "Run" button or press Ctrl+R
6. Choose an emulator or connected device

### Option 2: Install APK Directly
The built APK is located at:
```
BattleboatAndroid/app/build/outputs/apk/debug/app-debug.apk
```

#### On Physical Device:
1. Enable "Unknown Sources" in Android Settings > Security
2. Transfer the APK to your phone
3. Tap the APK file to install

#### On Emulator:
1. Start an Android emulator
2. Drag and drop the APK file onto the emulator

## 🎮 How to Play

1. **Start Game**: Tap "Play Classic Game" from main menu
2. **Ship Placement**: Ships are automatically placed (shown as dark gray squares)
3. **Your Turn**: Tap any cell on the enemy grid (top) to shoot
4. **Enemy Turn**: AI will automatically take its turn after yours
5. **Win Condition**: Sink all 5 enemy ships to win!

## 📱 Game Features

- **10x10 Interactive Grids**: A-J columns, 1-10 rows with coordinates
- **5 Ship Types**: Carrier (5), Battleship (4), Cruiser (3), Submarine (3), Destroyer (2)
- **Smart AI**: Uses probability algorithms and hunt patterns
- **Visual Feedback**: 
  - 🟦 Water (empty cells)
  - ⚫ Your ships (visible on your grid)
  - 🔴 Hits (red squares with white circles)
  - ⚪ Misses (white squares with blue circles)
  - 🔴⚫ Sunk ships (dark red)
- **Statistics Tracking**: Shots fired, accuracy, game time
- **Analytics**: All game events tracked to Amplitude with your API key

## 🔧 Analytics Dashboard

Your Amplitude events should now appear at:
- Dashboard: https://analytics.amplitude.com/
- API Key: `909b2239fab57efd4268eb75dbc28d30`

Events being tracked:
- Game Started/Ended
- Shot Fired (with position and result)
- Ship Sunk (with ship type)
- User engagement metrics

## 🐛 Troubleshooting

### If grids still don't show:
1. Check Android Studio logs for any errors
2. Ensure the GridView is properly initialized
3. Verify ship placement is working in logs

### If analytics don't appear in Amplitude:
1. Check internet connection
2. Verify API key is correct
3. Check console logs for "Analytics Event:" messages
4. Allow some time for events to appear in dashboard

### If app crashes:
1. Check Android Studio logs
2. Ensure device/emulator has sufficient memory
3. Try on different API level (24+ required)

## 📋 Build Info

- **Target SDK**: 33 (Android 13)
- **Min SDK**: 24 (Android 7.0)
- **Dependencies**: 
  - Amplitude Analytics SDK
  - AndroidX libraries
  - Material Design components

The app is fully functional and ready for testing! 🎉 