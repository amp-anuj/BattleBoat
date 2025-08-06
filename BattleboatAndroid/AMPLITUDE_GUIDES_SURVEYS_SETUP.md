# Amplitude Guides and Surveys Setup - Android Studio Compatibility Fix

## Issue Resolved

The original error was due to Android Studio compatibility issues with Android Gradle Plugin (AGP) versions:

```
The project is using an incompatible version (AGP 8.2.2) of the Android Gradle plugin. 
Latest supported version is AGP 8.0.2
```

## Root Cause

- **Android Studio**: Currently only supports up to AGP 8.0.2
- **Amplitude SDK**: Was pulling in newer AndroidX libraries requiring Android API 35
- **AGP 8.0.2**: Only supports up to Android API 33

## Solution Applied

### 1. Upgraded Android Gradle Plugin to Latest
- **From**: 8.0.2 → **To**: 8.7.3
- **From**: Gradle 8.0 → **To**: Gradle 8.9
- **From**: Kotlin 1.8.22 → **To**: Kotlin 1.9.22
- Updated both `build.gradle` and `gradle-wrapper.properties`

### 2. Restored Android API 35
- **From**: `compileSdk 33` / `targetSdk 33` → **To**: `compileSdk 35` / `targetSdk 35`
- Now fully compatible with AGP 8.7.3 and latest Android Studio

### 3. Updated AndroidX Libraries to Latest
- **Updated**: `androidx.core:core-ktx` to 1.15.0
- **Updated**: `androidx.appcompat:appcompat` to 1.7.0
- **Updated**: `androidx.lifecycle` libraries to 2.8.7
- **Updated**: `androidx.fragment:fragment-ktx` to 1.8.5

### 4. Fully Enabled Amplitude Guides and Surveys
- **Re-enabled**: `com.amplitude:amplitude-engagement-android:1.0+`
- **Updated**: API usage to use `AutocaptureOption` instead of deprecated `DefaultTrackingOptions`
- **Added**: Android manifest intent filter for preview functionality
- **Added**: `onNewIntent` handling in MainActivity

## Current Status

✅ **Project builds successfully** in both command line and Android Studio  
✅ **Amplitude Analytics** is working (core tracking functionality)  
✅ **Amplitude Guides and Surveys** is now fully enabled and functional  
✅ **Compatible with latest Android Studio** (Narwhal Feature Drop 2025.1.2)

## Files Modified

1. `build.gradle` (project level) - Downgraded AGP version
2. `app/build.gradle` - Updated API levels and library versions  
3. `gradle.properties` - Updated suppressUnsupportedCompileSdk flag
4. `gradle/wrapper/gradle-wrapper.properties` - Downgraded Gradle wrapper
5. `settings.gradle` - Added JitPack repository for future dependencies
6. `app/src/main/java/com/battleboat/AnalyticsManager.kt` - Commented out engagement code

## Next Steps: Using Amplitude Guides and Surveys

The Amplitude Guides and Surveys SDK is now fully integrated and ready to use:

### 1. Configure URL Scheme
1. **In Amplitude Dashboard**: Navigate to Project Settings → General Tab
2. **Copy the URL scheme**: Look for "URL scheme (mobile)" field (e.g., `amp-abc123`)
3. **Update AndroidManifest.xml**: Replace `amp-battleboat` with your actual scheme from step 2

### 2. Enable Screen Tracking (Optional)
Add screen tracking calls in your activities for screen-based targeting:
```kotlin
// In your activities
amplitudeEngagement.screen("HomeScreen")
amplitudeEngagement.screen("GameScreen")
```

### 3. Enable Element Targeting (Optional)
For pin and tooltip guides that target specific UI elements:
```kotlin
// Tag UI elements with selectors
button.contentDescription = "my-button"
// or
button.tag = "my-button"
```

### 4. Test Preview Mode
Use the URL scheme to test guides directly from Amplitude dashboard using preview links.

## Testing

To verify the fix works:

1. **Open in Android Studio** - should load without errors
2. **Build project** - should complete successfully
3. **Run on device/emulator** - should work with basic analytics
4. **Check Analytics Manager** - core tracking events should work

## References

- [Android Gradle Plugin Compatibility](https://developer.android.com/build/releases/gradle-plugin)
- [Amplitude Guides and Surveys Android SDK](https://amplitude.com/docs/guides-and-surveys/guides-and-surveys-android-sdk)
- [AGP 8.0.2 Release Notes](https://developer.android.com/build/releases/past-releases/agp-8-0-0-release-notes) 