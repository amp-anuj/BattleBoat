package com.battleboat

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import com.amplitude.android.Amplitude
import com.amplitude.android.Configuration
import com.amplitude.android.AutocaptureOption
import com.amplitude.android.TrackingOptions
import com.amplitude.android.engagement.AmplitudeEngagement
import com.amplitude.android.engagement.AmplitudeBootOptions
import com.amplitude.android.engagement.AmplitudeEndUser
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
    private var amplitudeEngagement: AmplitudeEngagement? = null
    private var isEngagementReady = false  // Track when JS engine is loaded
    
    // MARK: - Game Activity Reference for Callbacks
    var gameActivity: GameActivity? = null

    val options = NetworkTrackingOptions(
        captureRules = listOf(
        // Track all responses from your API domain with status code from 400 to 599
            CaptureRule(
                hosts = listOf("*.example.com", "example.com", "*.amplitude.com"),
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
            Log.d(TAG, "🔧 Creating AmplitudeEngagement instance...")
            Log.d(TAG, "📱 Context: ${context}")
            Log.d(TAG, "🔑 API Key length: ${key.length}")
            
            val amplitudeEngagement = AmplitudeEngagement(
                context = context,
                apiKey = key
            )
            
            // Element targeting is automatically supported for non-Jetpack Compose views
            // using tag, contentDescription, or resourceName fields
            Log.d(TAG, "🎯 Element targeting ready for Guides (using tag/contentDescription)")
            
            // Store the engagement instance for callbacks
            this.amplitudeEngagement = amplitudeEngagement
            Log.d(TAG, "✅ AmplitudeEngagement instance created successfully")
            
            // Note: Callbacks will be added after boot to avoid NullPointerException

            
            // Initialize Session Replay plugin
            // DISABLED: Compose Alpha support still has reflection access issues
            // Error: "Field 'androidx.compose.runtime.collection.MutableVector.content' is inaccessible"
            // Waiting for stable Compose support from Amplitude
            Log.d(TAG, "⚠️ Session Replay disabled - Jetpack Compose Alpha has known issues")
            /*
            Log.d(TAG, "🔧 Creating Session Replay plugin...")
            sessionReplayPlugin = SessionReplayPlugin(
                sampleRate = 1.0  // Record 100% of sessions for full coverage
            )
            Log.d(TAG, "✅ Session Replay plugin created successfully")
            */
            
            Log.d(TAG, "🔧 Creating main Amplitude instance...")
            amplitude = Amplitude(
                Configuration(
                    apiKey = key,
                    context = context,
                    autocapture = setOf(
                        AutocaptureOption.SESSIONS,
                        AutocaptureOption.APP_LIFECYCLES,
                        AutocaptureOption.FRUSTRATION_INTERACTIONS
                    ),
                    optOut = false
                )
            )
            Log.d(TAG, "✅ Main Amplitude instance created successfully")
            
            Log.d(TAG, "🔧 Adding plugins to Amplitude...")
            amplitude!!.add(amplitudeEngagement.getPlugin())
            Log.d(TAG, "✅ AmplitudeEngagement plugin added")
            // Session Replay disabled - Compose Alpha issues
            // amplitude!!.add(sessionReplayPlugin!!)
            // Log.d(TAG, "✅ Session Replay plugin added")
            amplitude!!.add(networkPlugin)
            Log.d(TAG, "✅ Network plugin added")
            
            // Set user ID if exists
            val userId = getUserId()
            if (userId.isNotEmpty()) {
                amplitude?.setUserId(userId)
                Log.d(TAG, "👤 User ID set: $userId")
            }
            val sessionId = amplitude!!.sessionId
            Log.d(TAG, "👤 Session ID set: $sessionId")
            isInitialized = true
            Log.d(TAG, "🏁 Analytics marked as initialized")
            
            // Note: AmplitudeEngagement now automatically syncs with Amplitude via the plugin
            // No need for manual boot() call - the plugin handles device/user ID sync
            Log.d(TAG, "✅ AmplitudeEngagement synced via plugin (no boot required)")
            
            // Delay callback setup to allow JS engine to fully initialize
            setupCallbacksDelayed()
            
            Log.d(TAG, "✅ Analytics initialized successfully!")
            Log.d(TAG, "👤 Session ID: ${amplitude!!.sessionId}")
            Log.d(TAG, "🎯 User ID: ${amplitude!!.getUserId() ?: "Anonymous"}")
            
            // Device ID is generated asynchronously - boot engagement after a short delay
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                val deviceId = amplitude?.getDeviceId()
                val userId = amplitude?.getUserId()
                Log.d(TAG, "👤 Device ID (async): ${deviceId ?: "not yet available"}")
                
                // Boot engagement with device/user IDs once they're available
                if (deviceId != null) {
                    Log.d(TAG, "🔧 Booting AmplitudeEngagement with Device ID...")
                    val bootOptions = AmplitudeBootOptions(
                        user = AmplitudeEndUser(
                            userId = userId,
                            deviceId = deviceId
                        )
                    )
                    amplitudeEngagement?.boot(bootOptions)
                    Log.d(TAG, "✅ AmplitudeEngagement booted with Device ID: $deviceId")
                } else {
                    Log.w(TAG, "⚠️ Device ID still not available, engagement may not work properly")
                }
            }, 1000)
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
     * Check if Amplitude is properly initialized
     */
    fun isAmplitudeInitialized(): Boolean {
        val initialized = isInitialized && amplitude != null && amplitudeEngagement != null
        Log.d(TAG, "🔍 Amplitude initialization check: $initialized (isInitialized=$isInitialized, amplitude=${amplitude != null}, engagement=${amplitudeEngagement != null})")
        return initialized
    }
    
    /**
     * Track a custom event
     */
    fun trackEvent(eventName: String, properties: Map<String, Any> = emptyMap()) {
        if (!isAnalyticsEnabled()) {
            Log.w(TAG, "📊 Analytics disabled - skipping event: $eventName")
            return
        }
        
        if (!isAmplitudeInitialized()) {
            Log.e(TAG, "❌ Amplitude not initialized - skipping event: $eventName")
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
     * Track screen views for Guides and Surveys targeting
     * Includes retry logic if engagement SDK is not ready yet
     */
    fun trackScreen(screenName: String, retryCount: Int = 0) {
        if (!isAnalyticsEnabled() || !isAmplitudeInitialized()) {
            return
        }
        
        // If engagement is not ready, retry after a delay (max 5 retries)
        if (!isEngagementReady && retryCount < 5) {
            Log.d(TAG, "⏳ Engagement not ready yet, retrying screen tracking for '$screenName' (attempt ${retryCount + 1}/5)...")
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                trackScreen(screenName, retryCount + 1)
            }, 1000) // Retry after 1 second
            return
        }
        
        try {
            amplitudeEngagement?.screen(screenName)
            Log.d(TAG, "📱 Screen tracked: $screenName")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to track screen: $screenName - ${e.message}")
            // If it fails due to JS not being ready, retry once more
            if (e.message?.contains("publish") == true && retryCount < 5) {
                Log.d(TAG, "⏳ JS engine not ready, retrying in 2 seconds...")
                isEngagementReady = false
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    trackScreen(screenName, retryCount + 1)
                }, 2000)
            }
        }
    }
    
    /**
     * Track game start - matches standardized naming
     */
    fun trackStartGame() {
        trackEvent("Game Started")
    }

    /**
     * Track game end - matches standardized naming
     */
    fun trackGameEnd(playerWon: Boolean, shotsTaken: Int) {
        trackEvent("Game Ended", mapOf(
            "Win" to playerWon,
            "Shots Taken" to shotsTaken
        ))
    }

    /**
     * Track shot fired - matches standardized naming
     */
    fun trackPlayerShoot(x: Int, y: Int, hit: Boolean, consecutiveHits: Int = 0) {
        trackEvent("Shot Fired", mapOf(
            "X" to x,
            "Y" to y,
            "Hit" to hit,
            "Consecutive Hits" to consecutiveHits,
            "Player" to "human"
        ))
    }

    /**
     * Track ship selection - matches standardized naming
     */
    fun trackSelectShip(shipType: String) {
        trackEvent("Ship Selected", mapOf(
            "Ship" to shipType
        ))
    }

    /**
     * Track ship placement - matches standardized naming
     */
    fun trackPlaceShip(shipType: String, success: Boolean, x: Int? = null, y: Int? = null) {
        val properties = mutableMapOf<String, Any>(
            "Ship" to shipType,
            "Success" to success
        )
        
        if (x != null && y != null) {
            properties["X"] = x
            properties["Y"] = y
        }
        
        trackEvent("Ship Placed", properties)
    }

    /**
     * Track ship rotation - matches standardized naming
     */
    fun trackRotateShip(shipType: String) {
        trackEvent("Ship Rotated", mapOf(
            "Ship" to shipType
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
     * Guides and Surveys Preview Link Handling
     */
    fun handlePreviewLink(intent: Intent): Boolean {
        return amplitudeEngagement?.handlePreviewLinkIntent(intent) ?: true
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
                val deviceId = amplitude!!.getDeviceId()
                status.appendLine("👤 Device ID: ${deviceId ?: "(generating...)"}")
                status.appendLine("🎯 User ID: ${amplitude!!.getUserId() ?: "Anonymous"}")
                status.appendLine("🎯 Engagement Ready: $isEngagementReady")
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
    
    /**
     * Setup callbacks with delay to allow JS engine to fully initialize
     * This prevents NullPointerException during initialization
     */
    private fun setupCallbacksDelayed() {
        // Use a handler to delay callback setup by 3 seconds to allow JS engine to fully load
        // The "Engagement bundle loaded" log indicates when the JS engine is ready
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            Log.d(TAG, "⏳ Checking if Engagement JS engine is ready...")
            isEngagementReady = true
            setupCallbacks()
        }, 3000)
    }
    
    /**
     * Setup callbacks after AmplitudeEngagement is properly booted
     * This prevents NullPointerException during initialization
     */
    private fun setupCallbacks(retryCount: Int = 0) {
        try {
            Log.d(TAG, "🔧 Setting up Amplitude callbacks (attempt ${retryCount + 1})...")
            
            // Re-enable callbacks now that JS engine is loaded ('Engagement bundle loaded')
            Log.d(TAG, "🎯 JavaScript engine is ready, attempting to add callbacks...")
            
            amplitudeEngagement?.addCallback("place_ship") {
                Log.d(TAG, "🎯 Amplitude Guide Callback: Placing carrier ship horizontally at second position")
                
                val activity = gameActivity
                if (activity == null) {
                    Log.e(TAG, "❌ GameActivity not available for callback")
                    return@addCallback
                }
                
                // Check if we're in the ship placement phase
                if (activity.getCurrentGameState() != GameState.SETUP) {
                    Log.e(TAG, "❌ Not in ship placement phase - current state: ${activity.getCurrentGameState()}")
                    return@addCallback
                }
                
                // Get the carrier ship from the player fleet
                val carrierShip = activity.getPlayerFleet().getShip(ShipType.CARRIER)
                if (carrierShip == null) {
                    Log.e(TAG, "❌ Carrier ship not found in fleet")
                    return@addCallback
                }
                
                // Check if carrier is already placed
                if (carrierShip.isPlaced) {
                    Log.d(TAG, "ℹ️ Carrier already placed, skipping callback action")
                    return@addCallback
                }
                
                // Place the ship at second position (1,1) - horizontal placement
                val row = 1  // Second row (0-indexed)
                val col = 1  // Second column (0-indexed)
                val orientation = Orientation.HORIZONTAL
                
                val success = activity.getPlayerFleet().placeShip(carrierShip, row, col, orientation)
                
                if (success) {
                    // Update the grid
                    activity.getPlayerGrid().placeShip(carrierShip)
                    
                    // Move to the next unplaced ship (skip the carrier that was just placed)
                    activity.moveToNextUnplacedShip()
                    
                    // Refresh the UI
                    activity.refreshUI()
                    
                    Log.d(TAG, "✅ Carrier successfully placed horizontally at position ($col, $row) via Amplitude Guide!")
                    Log.d(TAG, "🎯 Moving to next ship: ${activity.getCurrentShipType()?.displayName ?: "All ships placed"}")
                    
                    // Track the callback action
                    trackEvent("Ship Placed", mapOf(
                        "Ship" to "carrier",
                        "X" to col,
                        "Y" to row,
                        "Success" to true,
                        "Source" to "amplitude_guides_surveys"
                    ))
                } else {
                    Log.e(TAG, "❌ Failed to place carrier at position ($col, $row) - position might be occupied or invalid")
                }
            }
            
            Log.d(TAG, "✅ Callbacks setup completed successfully")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to setup callbacks: ${e.message}")
            
            // If it's a "publish" error, the JS engine isn't ready - retry
            if (e.message?.contains("publish") == true && retryCount < 3) {
                Log.d(TAG, "⏳ JS engine not ready for callbacks, retrying in 2 seconds...")
                isEngagementReady = false
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    isEngagementReady = true
                    setupCallbacks(retryCount + 1)
                }, 2000)
            }
        }
    }
} 