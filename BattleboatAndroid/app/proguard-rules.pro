# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Amplitude Analytics and Engagement SDK
-keep class com.amplitude.** { *; }
-keep class com.amplitude.android.** { *; }
-keep class com.amplitude.android.engagement.** { *; }
-keepclassmembers class com.amplitude.** { *; }

# Keep all assets including engagement.js
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Preserve JavaScript interface methods
-keepattributes JavascriptInterface

# Keep WebView related classes (Amplitude Guides use WebView)
-keep class android.webkit.** { *; }
-keep class * extends android.webkit.WebViewClient
-keep class * extends android.webkit.WebChromeClient

# Keep assets and resources
-keepclassmembers class **.R$* {
    public static <fields>;
}

# Session Replay Plugin
-keep class com.amplitude.android.plugins.** { *; }

# Keep Compose runtime classes for Session Replay
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.runtime.collection.** { *; }
-keepclassmembers class androidx.compose.runtime.collection.MutableVector {
    private ** content;
    public ** getContent();
}

# Don't warn about Compose internals
-dontwarn androidx.compose.runtime.**
-dontwarn androidx.compose.runtime.collection.**
