package com.battleboat

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import com.amplitude.android.Amplitude
import com.amplitude.android.Configuration
import com.amplitude.android.AutocaptureOption
import com.amplitude.android.TrackingOptions
import com.amplitude.android.engagement.AmplitudeBootOptions
import com.amplitude.android.engagement.AmplitudeEndUser
import com.amplitude.android.engagement.AmplitudeEngagement
import com.amplitude.android.plugins.SessionReplayPlugin
import com.amplitude.core.network.NetworkTrackingPlugin
import com.amplitude.core.network.NetworkTrackingOptions
import com.amplitude.core.network.NetworkTrackingOptions.CaptureRule
import com.amplitude.core.events.Identify
import okhttp3.OkHttpClient

/**
 * Analytics manager using Amplitude for user engagement tracking
 */
class AnalyticsManager private constructor(private val context: Context) {
    
    companion object {
        private const val TAG = "AnalyticsManager"
        private const val API_KEY = "909b2239fab57efd4268eb75dbc28d30" // Real API key
        private const val PREFS_NAME = "analytics_prefs"
        private const val KEY_ANALYTICS_ENABLED = "analytics_enabled"
        private const val KEY_USER_ID = "user_id"
        
        @Volatile
        private var INSTANCE: AnalyticsManager? = null
        
        fun getInstance(context: Context): AnalyticsManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AnalyticsManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private var amplitude: Amplitude? = null
    private var sessionReplayPlugin: SessionReplayPlugin? = null
    private var isInitialized = false

    val options = NetworkTrackingOptions(
        captureRules = listOf(
        // Track all responses from your API domain with status code from 400 to 599
            CaptureRule(
                hosts = listOf("*.example.com", "example.com"),
                statusCodeRange = (400..599).toList()
            )
        ),
        // Ignore specific domains
        ignoreHosts = listOf("analytics.example.com", "*.internal.com"),
        // Whether to ignore Amplitude API requests
        ignoreAmplitudeRequests = false
    )
    private val networkPlugin = NetworkTrackingPlugin(options)

    // Add the plugin as an interceptor to your OkHttp client
    val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(networkPlugin)
        .build()
    
    /**
     * Initialize analytics with user consent
     */
    fun initialize(apiKey: String? = null) {
        if (isInitialized) {
            Log.d(TAG, "Analytics already initialized")
            return
        }
        
        val key = apiKey ?: API_KEY
        if (key.isBlank()) {
            Log.e(TAG, "⚠️ No valid Amplitude API key provided! Analytics disabled.")
            Log.e(TAG, "📝 Please set your API key in AnalyticsManager.kt or pass it to initialize()")
            Log.e(TAG, "🔑 Get your API key from: https://amplitude.com -> Project Settings -> General")
            return
        }
        
        Log.d(TAG, "🚀 Initializing Amplitude Analytics...")
        Log.d(TAG, "📊 API Key: ${key.take(8)}...")
        
        try {
            val amplitudeEngagement = AmplitudeEngagement(
                context = context,
                apiKey = key
            )
            
            // Initialize Session Replay plugin
            sessionReplayPlugin = SessionReplayPlugin(
                sampleRate = 1.0  // Record 100% of sessions for full coverage
            )
            
            amplitude = Amplitude(
                Configuration(
                    apiKey = key,
                    context = context,
                    autocapture = setOf(
                        AutocaptureOption.SESSIONS,
                        AutocaptureOption.APP_LIFECYCLES,
                        AutocaptureOption.SCREEN_VIEWS
                    ),
                    optOut = false
                )
            )
            amplitude!!.add(amplitudeEngagement.getPlugin())
            amplitude!!.add(sessionReplayPlugin!!)
            amplitude!!.add(networkPlugin)
            
            // Set user ID if exists
            val userId = getUserId()
            if (userId.isNotEmpty()) {
                amplitude?.setUserId(userId)
            }
            
            isInitialized = true

            val bootOptions = AmplitudeBootOptions(
                user = AmplitudeEndUser(
                    userId = amplitude!!.getUserId(),
                    deviceId = amplitude!!.getDeviceId()
                )
            )
            amplitudeEngagement.boot(bootOptions)
            
            Log.d(TAG, "✅ Analytics initialized successfully!")
            Log.d(TAG, "👤 Device ID: ${amplitude!!.getDeviceId()}")
            Log.d(TAG, "🎯 User ID: ${amplitude!!.getUserId() ?: "Anonymous"}")
            Log.d(TAG, "📹 Session Replay: Recording 100% of sessions")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to initialize Amplitude Analytics", e)
            e.printStackTrace()
        }
    }
    
    /**
     * Enable or disable analytics
     */
    fun setAnalyticsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ANALYTICS_ENABLED, enabled).apply()
        
        if (enabled && !isInitialized) {
            initialize()
        }
    }
    
    /**
     * Check if analytics is enabled
     */
    fun isAnalyticsEnabled(): Boolean {
        return prefs.getBoolean(KEY_ANALYTICS_ENABLED, true) // Default to enabled
    }
    
    /**
     * Track a custom event
     */
    fun trackEvent(eventName: String, properties: Map<String, Any> = emptyMap()) {
        if (!isAnalyticsEnabled()) {
            Log.w(TAG, "📊 Analytics disabled - skipping event: $eventName")
            return
        }
        
        if (amplitude == null) {
            Log.e(TAG, "❌ Analytics not initialized - skipping event: $eventName")
            Log.e(TAG, "💡 Make sure to call analyticsManager.initialize() first")
            return
        }
        
        try {
            amplitude?.track(eventName, properties)
            // Force immediate flush for debugging
            amplitude?.flush()
            Log.d(TAG, "📈 Event tracked: $eventName ${if (properties.isNotEmpty()) "- $properties" else ""}")
            Log.d(TAG, "💫 Forced flush to send events immediately")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to track event: $eventName", e)
            e.printStackTrace()
        }
    }
    
    /**
     * Track game start - matches JS: amplitude.track('Start Game')
     */
    fun trackStartGame() {
        trackEvent("Start Game")
    }
    
    /**
     * Track game end - matches JS: amplitude.track('Game Over', {win: true/false, shotsTaken: shots})
     */
    fun trackGameOver(win: Boolean, shotsTaken: Int) {
        trackEvent("Game Over", mapOf(
            "win" to win,
            "shotsTaken" to shotsTaken
        ))
    }
    
    /**
     * Track shot fired - matches JS: amplitude.track('Shoot Ship', {x: x, y: y, hit: hit, consecutiveHits: hits})
     */
    fun trackShootShip(x: Int, y: Int, hit: Boolean, consecutiveHits: Int = 0) {
        trackEvent("Shoot Ship", mapOf(
            "x" to x,
            "y" to y,
            "hit" to hit,
            "consecutiveHits" to consecutiveHits
        ))
    }
    
    /**
     * Track ship selection - matches JS: amplitude.track('Select Ship', {ship: shipType, ship2: shipType})
     */
    fun trackSelectShip(ship: String) {
        trackEvent("Select Ship", mapOf(
            "ship" to ship,
            "ship2" to ship
        ))
    }
    
    /**
     * Track ship placement - matches JS: amplitude.track('Place Ship', {ship: shipType, success: success})
     */
    fun trackPlaceShip(ship: String, success: Boolean) {
        trackEvent("Place Ship", mapOf(
            "ship" to ship,
            "success" to success
        ))
    }
    
    /**
     * Track ship rotation - matches JS: amplitude.track('Rotate Ship', {ship: shipType})
     */
    fun trackRotateShip(ship: String) {
        trackEvent("Rotate Ship", mapOf(
            "ship" to ship
        ))
    }
    
    /**
     * Set user ID for analytics tracking
     */
    fun setUserId(userId: String) {
        prefs.edit().putString(KEY_USER_ID, userId).apply()
        amplitude?.setUserId(userId)
        Log.d(TAG, "👤 User ID set: $userId")
    }
    
    /**
     * Get current user ID
     */
    private fun getUserId(): String {
        return prefs.getString(KEY_USER_ID, "") ?: ""
    }
    
    /**
     * Check network connectivity
     */
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                   capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                   capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo?.isConnected == true
        }
    }
    
    /**
     * Force flush all pending events
     */
    fun flushEvents() {
        try {
            amplitude?.flush()
            Log.d(TAG, "💫 Manually flushed all pending events")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to flush events", e)
        }
    }
    
    /**
     * Session Replay status - runs automatically when initialized
     */
    fun getSessionReplayStatus(): String {
        return if (sessionReplayPlugin != null) {
            "✅ Session Replay: Active (100% capture rate)"
        } else {
            "❌ Session Replay: Not initialized"
        }
    }
    
    /**
     * Get detailed analytics status for debugging
     */
    fun getAnalyticsStatus(): String {
        val status = StringBuilder()
        status.appendLine("🔍 AMPLITUDE ANALYTICS DEBUG STATUS")
        status.appendLine("=".repeat(50))
        status.appendLine("🔑 API Key: ${API_KEY.take(8)}...")
        status.appendLine("✅ Analytics Enabled: ${isAnalyticsEnabled()}")
        status.appendLine("✅ Analytics Initialized: $isInitialized")
        status.appendLine("✅ Amplitude Instance: ${amplitude != null}")
        status.appendLine("📹 Session Replay: ${if (sessionReplayPlugin != null) "Active (100% capture)" else "Not initialized"}")
        status.appendLine("🌐 Network Available: ${isNetworkAvailable()}")
        
        if (amplitude != null) {
            try {
                status.appendLine("👤 Device ID: ${amplitude!!.getDeviceId()}")
                status.appendLine("🎯 User ID: ${amplitude!!.getUserId() ?: "Anonymous"}")
                // Note: optOut property might not be publicly accessible in this SDK version
                // status.appendLine("🚫 Opt Out Status: ${amplitude!!.optOut}")
            } catch (e: Exception) {
                status.appendLine("❌ Error getting Amplitude details: ${e.message}")
            }
        }
        
        status.appendLine("=".repeat(50))
        return status.toString()
    }
    
    /**
     * Test analytics integration - call this to verify everything works
     */
    fun testAnalytics() {
        val status = getAnalyticsStatus()
        Log.d(TAG, status)
        println(status) // Also print to console for easier debugging
        
        if (!isNetworkAvailable()) {
            Log.e(TAG, "❌ NO NETWORK - Events will be queued until network is available")
            return
        }
        
        if (amplitude == null) {
            Log.e(TAG, "❌ AMPLITUDE NOT INITIALIZED - Cannot track events")
            return
        }
        
        // Track multiple test events
        val testEvents = listOf(
            "Analytics Test" to mapOf(
                "timestamp" to System.currentTimeMillis(),
                "test_type" to "integration_test",
                "sdk_version" to "1.22.0",
                "platform" to "Android"
            ),
            "Debug Event 1" to mapOf("debug" to true, "event_number" to 1),
            "Debug Event 2" to mapOf("debug" to true, "event_number" to 2)
        )
        
        testEvents.forEach { (eventName, properties) ->
            trackEvent(eventName, properties)
        }
        
        // Force flush
        flushEvents()
        
        Log.d(TAG, "🧪 Test completed! Check Amplitude dashboard in 5-10 minutes")
        Log.d(TAG, "💡 If events don't appear, the issue is likely:")
        Log.d(TAG, "   1. API key mismatch with your Amplitude project")
        Log.d(TAG, "   2. Network/firewall blocking requests to api2.amplitude.com")
        Log.d(TAG, "   3. App is in debug mode and events are filtered")
    }
} 